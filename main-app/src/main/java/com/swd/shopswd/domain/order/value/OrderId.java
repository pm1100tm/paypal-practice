package com.swd.shopswd.domain.order.value;

import java.util.Objects;
import java.util.UUID;

/**
 * 주문 식별자.
 * 비회원 주문이므로 추측 불가능한 랜덤 UUID 를 사용한다 (주문 완료 페이지 조회 키).
 * PayPal purchase_unit 의 custom_id / invoice_id 로도 사용된다.
 */
public record OrderId(String value) {

    public OrderId {
        Objects.requireNonNull(value, "OrderId must not be null");
        try {
            value = UUID.fromString(value).toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid OrderId format: " + value, e);
        }
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }
}
