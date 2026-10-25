# shopswd-app

비회원 **장바구니 → 체크아웃 → PayPal 결제** 만 담은 백엔드 (Spring Boot 4 / Java 25).
회원가입·로그인·세션 없이, 서버가 발급한 추측 불가능한 `cartId`/`orderId`(UUID) 로 리소스를 식별합니다.

## 아키텍처 — Hexagonal + DDD

```plaintext
com.swd.shopswd
├── adapter/
│   ├── in/web/                 # Inbound Adapter
│   │   ├── api/                # REST Controllers (Product, Cart, Order, Payment)
│   │   ├── dto/                # Request/Response DTOs
│   │   └── exception/          # GlobalExceptionHandler
│   └── out/
│       ├── persistence/        # JPA Adapters (product, cart, order, payment)
│       └── external/payment/paypal/   # PayPal Orders v2 Adapter (PaymentGateway 구현)
├── application/
│   ├── port/in/                # UseCase Interfaces + Command/Result
│   ├── port/out/payment/       # PaymentGateway (Outbound Port)
│   └── service/                # UseCase 구현
├── domain/                     # 순수 Java (Spring/JPA/Jackson 의존 금지 — ArchUnit 으로 검증)
│   ├── product/  cart/  order/  payment/
│   └── shared/                 # Money, DomainException
└── config/                     # Clock, CORS, 배송비 정책 빈
```

의존성 방향 `Adapter → Application → Domain` 은 `HexagonalArchitectureTest`(ArchUnit) 가 강제합니다.

## 결제 흐름

```plaintext
[FE] PayPal 버튼 onClick   : 배송지 폼 검증
[FE] createOrder 콜백      → POST /api/v2/orders            Order(PENDING_PAYMENT) — 금액은 서버가 장바구니로 계산
                           → POST /api/v2/payments          PayPal Create Order → paymentId(=PayPal Order ID) 반환
[FE] PayPal 팝업 승인
[FE] onApprove             → POST /api/v2/payments/capture  PayPal Capture → 금액/ID 검증 → Order(PAID), 장바구니 비움
[FE] onCancel / onError    → POST /api/v2/payments/{cancel|error}/paypal  → Order(CANCELLED), 장바구니 유지
```

안전장치

- 금액은 클라이언트 값을 받지 않음. 캡처 금액·통화·PayPal Order ID·`custom_id` 가 주문과 정확히 일치해야 결제 완료
- 캡처는 됐는데 검증이 실패하면 `REVIEW_REQUIRED` 로 남김 (주문은 취소하지 않음 → 수동 확인/환불 대상)
- `PayPal-Request-Id` 멱등키 + 5xx/네트워크 재시도(최대 3회), 캡처 재요청은 멱등 처리
- `@Version` 낙관적 락으로 동시 캡처/취소 감지
- PayPal 요청/응답 본문(주소·이메일 포함)은 로그에 남기지 않음

## API

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/v2/products` | 상품 목록 |
| POST | `/api/v2/carts` | 비회원 장바구니 발급 → `cartId` |
| GET | `/api/v2/carts/{cartId}` | 장바구니 조회 |
| POST | `/api/v2/carts/{cartId}/items` | 담기 `{productId, quantity}` |
| PATCH | `/api/v2/carts/{cartId}/items/{productId}` | 수량 변경 `{quantity}` |
| DELETE | `/api/v2/carts/{cartId}/items/{productId}` | 삭제 |
| POST | `/api/v2/orders/checkout` | 금액 미리보기 `{cartId}` |
| POST | `/api/v2/orders` | 주문 생성 `{cartId, email, phone?, shippingAddress}` |
| GET | `/api/v2/orders/{orderId}` | 주문 조회 (개인정보 최소 노출) |
| POST | `/api/v2/payments` | PayPal 주문 생성 `{orderId, paymentMethod:"PAYPAL"}` |
| POST | `/api/v2/payments/capture` | 캡처 `{orderId, paymentId}` |
| POST | `/api/v2/payments/cancel/paypal` | 결제 전 취소 `{orderId}` |
| POST | `/api/v2/payments/error/paypal` | SDK 오류 기록 `{orderId, errorName?, errorMessage?}` |

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 실행

앱은 기본으로 **로컬 MySQL(`localhost:3306/shopswd`, `shopswd`/`shopswd`)** 에 접속합니다. MySQL 은 docker compose 로 띄웁니다.

```bash
cp .env.template .env      # PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET 입력 (커밋 금지)
docker compose up -d       # MySQL 8.4 — 최초 기동 시 스키마 + 기본 데이터(src/main/resources/db/data.sql)
set -a && source .env && set +a
./gradlew bootRun          # 또는 IntelliJ 에서 ShopSwdApplication 실행 (PayPal 환경변수만 지정)
```

MySQL 이 떠 있지 않으면 앱이 기동되지 않습니다. H2 는 테스트(`profile: test`) 전용입니다.

PayPal 키가 없어도 서버는 뜨고 장바구니/주문 API 는 동작합니다. 결제 생성 시에만 `PAYPAL_NOT_CONFIGURED` (502) 가 반환됩니다.

| 환경변수 | 기본값 | 설명 |
| --- | --- | --- |
| `PAYPAL_BASE_URL` | `https://api-m.sandbox.paypal.com` | Live: `https://api-m.paypal.com` |
| `PAYPAL_CLIENT_ID` / `PAYPAL_CLIENT_SECRET` | (없음) | PayPal REST App 자격증명 |
| `MAIN_MOBILE_PUBLIC_ENDPOINT` | `http://localhost:3000` | 프론트 주소 (CORS, return/cancel URL) |
| `CHECKOUT_SHIPPING_FLAT_FEE` | `0.00` | 정액 배송비 (USD) |
| `CHECKOUT_SHIPPING_FREE_THRESHOLD` | (없음) | 무료배송 기준금액 (USD) |
| `RDB_URL` / `RDB_USER` / `RDB_PASSWORD` | `jdbc:mysql://localhost:3306/shopswd` / `shopswd` / `shopswd` | 다른 DB 사용 시 덮어쓰기 (`MYSQL_PORT` 를 바꿨다면 `RDB_URL` 포트도 맞출 것) |
| `JPA_DDL_AUTO` / `SQL_INIT_MODE` | `validate` / `never` | 스키마·기본 데이터는 MySQL 초기화 스크립트가 담당 |

## Docker (MySQL) 실행

```bash
cp .env.template .env        # PayPal 키 입력. compose 가 같은 디렉터리의 .env 를 자동으로 읽음
docker compose up -d --build # MySQL 8.4 (+ App: compose.yaml 의 app 서비스 주석 해제 시)
curl localhost:8080/api/v2/products
```

- MySQL 볼륨이 **처음 생성될 때** `docker/mysql/init/01-schema.sql`(스키마) → `src/main/resources/db/data.sql`(기본 데이터) 순서로 들어갑니다.
- 앱은 `JPA_DDL_AUTO=validate` 로 스키마를 검증만 합니다. 엔티티를 바꾸면 `01-schema.sql` 도 같이 바꿔야 합니다.
- 데이터는 `mysql-data` 볼륨에 남습니다. 기본 데이터부터 다시 시작하려면 `docker compose down -v && docker compose up -d --build`.
- 앱 로그: `docker compose logs -f app` / DB 접속: `docker compose exec mysql mysql -ushopswd -pshopswd shopswd`

| 환경변수 (.env) | 기본값 | 설명 |
| --- | --- | --- |
| `MYSQL_PORT` | `3306` | 호스트에 노출할 MySQL 포트 (로컬 MySQL 과 겹치면 변경) |
| `MYSQL_DATABASE` / `MYSQL_USER` / `MYSQL_PASSWORD` | `shopswd` / `shopswd` / `shopswd` | 앱이 접속하는 DB 계정 |
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root 비밀번호 |
| `APP_PORT` | `8080` | 호스트에 노출할 앱 포트 |

기본 계정/비밀번호는 로컬 개발용입니다. 공유·운영 환경에서는 `.env` 에서 반드시 바꾸세요.

## 테스트

```bash
./gradlew test
```

PayPal 은 호출하지 않습니다 — `PaymentGateway` 를 Mock 으로 대체한 API 흐름 테스트, 도메인 단위 테스트,
PayPal 요청/응답 변환 테스트, ArchUnit 아키텍처 테스트로 구성되어 있습니다.

## 범위 밖 (v1)

회원/로그인/세션, 쿠폰·포인트·제휴, 세금(관부가세)·국가별 배송비, 재고 차감, 환불, Vault(저장 결제수단), 웹훅, 다중 통화(USD 고정).
