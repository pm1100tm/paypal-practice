-- ==========================================================================
-- ShopSWD 스키마 (MySQL 8.x)
--  - docker compose 로 MySQL 볼륨이 처음 만들어질 때 1회 실행된다.
--  - JPA 엔티티(adapter/out/persistence)와 일치해야 한다 — 앱은 ddl-auto=validate 로 검증만 한다.
-- ==========================================================================

CREATE TABLE product (
  id         BIGINT        NOT NULL,
  name       VARCHAR(255)  NOT NULL,
  brand      VARCHAR(100)  NULL,
  price      DECIMAL(12,2) NOT NULL,
  currency   VARCHAR(3)    NOT NULL,
  image_url  VARCHAR(500)  NULL,
  status     VARCHAR(20)   NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart (
  id          VARCHAR(36) NOT NULL,
  created_at  DATETIME(6) NOT NULL,
  updated_at  DATETIME(6) NOT NULL,
  version     BIGINT      NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_item (
  cart_id     VARCHAR(36) NOT NULL,
  line_no     INT         NOT NULL,
  product_id  BIGINT      NOT NULL,
  quantity    INT         NOT NULL,
  PRIMARY KEY (cart_id, line_no),
  CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
  id                  VARCHAR(36)   NOT NULL,
  cart_id             VARCHAR(36)   NOT NULL,
  currency            VARCHAR(3)    NOT NULL,
  item_total          DECIMAL(12,2) NOT NULL,
  shipping_cost       DECIMAL(12,2) NOT NULL,
  total_amount        DECIMAL(12,2) NOT NULL,
  email               VARCHAR(255)  NOT NULL,
  phone               VARCHAR(50)   NULL,
  ship_full_name      VARCHAR(300)  NOT NULL,
  ship_address_line1  VARCHAR(300)  NOT NULL,
  ship_address_line2  VARCHAR(300)  NULL,
  ship_city           VARCHAR(120)  NOT NULL,
  ship_state          VARCHAR(300)  NULL,
  ship_postal_code    VARCHAR(60)   NULL,
  ship_country_code   VARCHAR(2)    NOT NULL,
  status              VARCHAR(30)   NOT NULL,
  cancel_reason       VARCHAR(30)   NULL,
  created_at          DATETIME(6)   NOT NULL,
  paid_at             DATETIME(6)   NULL,
  cancelled_at        DATETIME(6)   NULL,
  version             BIGINT        NULL,
  PRIMARY KEY (id),
  KEY idx_orders_cart_id (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_item (
  order_id      VARCHAR(36)   NOT NULL,
  line_no       INT           NOT NULL,
  product_id    BIGINT        NOT NULL,
  product_name  VARCHAR(255)  NOT NULL,
  unit_price    DECIMAL(12,2) NOT NULL,
  quantity      INT           NOT NULL,
  PRIMARY KEY (order_id, line_no),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment (
  id                 VARCHAR(36)   NOT NULL,
  order_id           VARCHAR(36)   NOT NULL,
  method             VARCHAR(20)   NOT NULL,
  currency           VARCHAR(3)    NOT NULL,
  amount             DECIMAL(12,2) NOT NULL,
  status             VARCHAR(30)   NOT NULL,
  pg_order_id        VARCHAR(64)   NULL,
  capture_id         VARCHAR(64)   NULL,
  captured_amount    DECIMAL(12,2) NULL,
  captured_currency  VARCHAR(3)    NULL,
  pg_fee             DECIMAL(12,2) NULL,
  pg_custom_id       VARCHAR(127)  NULL,
  failure_code       VARCHAR(100)  NULL,
  failure_message    VARCHAR(1000) NULL,
  created_at         DATETIME(6)   NOT NULL,
  updated_at         DATETIME(6)   NOT NULL,
  version            BIGINT        NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_order_id (order_id),
  KEY idx_payment_pg_order_id (pg_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
