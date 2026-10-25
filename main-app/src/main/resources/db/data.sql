-- 기본 데이터: 데모 상품 (USD). 실제 상품 데이터로 교체해서 사용한다.
-- - MySQL(docker compose): 볼륨 최초 생성 시 /docker-entrypoint-initdb.d/02-data.sql 로 실행
-- - 테스트(H2, profile: test): 컨텍스트 기동 시 spring.sql.init 으로 실행
INSERT INTO product (id, name, brand, price, currency, image_url, status) VALUES
  (1001, 'Hydrating Toner 200ml',          'Demo Brand A', 18.50, 'USD', NULL, 'ON_SALE'),
  (1002, 'Vitamin C Serum 30ml',           'Demo Brand B', 24.00, 'USD', NULL, 'ON_SALE'),
  (1003, 'Daily Sunscreen SPF50+ 50ml',    'Demo Brand A', 15.90, 'USD', NULL, 'ON_SALE'),
  (1004, 'Cica Repair Cream 50ml',         'Demo Brand C', 21.00, 'USD', NULL, 'ON_SALE'),
  (1005, 'Sheet Mask Set (10 pcs)',        'Demo Brand B', 12.00, 'USD', NULL, 'ON_SALE'),
  (1006, 'Limited Edition Lip Tint',       'Demo Brand C',  9.80, 'USD', NULL, 'SOLD_OUT');
