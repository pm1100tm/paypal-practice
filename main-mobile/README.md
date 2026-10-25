# sk-main-mobile-v1

비회원 **장바구니 → 체크아웃 → PayPal 결제** 프론트엔드 (Next.js 16 / React 19, FSD).
로그인·세션·쿠키 없이 서버가 발급한 `cartId` 를 `localStorage` 에 보관해 장바구니를 유지합니다.

## 아키텍처 — FSD (Feature-Sliced Design)

```plaintext
src/
├── app/                         # Next.js App Router + FSD app 레이어
│   ├── (providers)/QueryProvider.tsx
│   ├── page.tsx                 # 상품 목록
│   └── (purchase-flow)/
│       ├── cart/                # 장바구니
│       ├── order/checkout/      # 체크아웃 (PAYPAL_CLIENT_ID 런타임 주입)
│       └── order-complete/      # 주문 완료
├── widgets/                     # header, product-list, cart, checkout, order-complete
├── features/
│   ├── cart/                    # 담기 / 수량 변경 / 삭제
│   ├── checkout-address/        # 연락처·배송지 폼 (zod + react-hook-form)
│   └── paypal-checkout/         # PayPal SDK Provider / 버튼 / 이벤트 핸들러
├── entities/                    # product, cart(cartId 보관), order, payment — API·모델·UI
└── shared/                      # api(baseFetch), config(routes), lib(money, toast, cn), ui
```

레이어 규칙은 `npm run steiger` 로 검사합니다. (사용처가 1곳인 슬라이스를 경고하는 `insignificant-slice` 휴리스틱만 비활성화)

## PayPal 흐름

`features/paypal-checkout/lib/usePayPalSDKEventHandlers.ts`

```plaintext
onClick     배송지 폼 검증 (실패 시 actions.reject → 팝업 안 열림)
createOrder POST /orders → POST /payments → PayPal Order ID 를 SDK 에 반환
onApprove   POST /payments/capture (캡처는 서버에서) → 장바구니 갱신 → /order-complete?id=...
onCancel    POST /payments/cancel/paypal → 체크아웃 화면 유지 (장바구니 보존, 바로 재시도 가능)
onError     POST /payments/error/paypal  → 체크아웃 화면 유지
```

## 실행

```bash
nvm use                       # Node 24
npm install
cp .env.template .env.local   # PAYPAL_CLIENT_ID 입력 (백엔드와 같은 PayPal REST App)
npm run dev                   # http://localhost:3000  (백엔드는 http://localhost:8080)
```

| 환경변수 | 기본값 | 설명 |
| --- | --- | --- |
| `MAIN_APP_ENDPOINT` | `http://localhost:8080` | 백엔드 주소. `/api/v2/*` rewrites 대상 — **`next build` 시점에 반영**되므로 빌드 환경에도 지정 |
| `PAYPAL_CLIENT_ID` | (없음) | 요청 시점에 서버에서 읽어 SDK 에 전달. 없으면 결제 버튼 대신 설정 안내 표시 |

## 스크립트

| 명령 | 설명 |
| --- | --- |
| `npm run dev` / `build` / `start` | 개발 서버 / 빌드 (standalone) / 실행 |
| `npm run typecheck` | TypeScript 검사 |
| `npm run steiger` | FSD 아키텍처 린트 |
| `npm test` | Vitest 단위 테스트 |
