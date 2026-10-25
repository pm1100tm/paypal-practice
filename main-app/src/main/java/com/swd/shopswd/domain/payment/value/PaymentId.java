package com.swd.shopswd.domain.payment.value;

import java.util.Objects;
import java.util.UUID;

/**
 * 내부 결제 식별자 (PG 주문 ID 와 별개).
 */
public record PaymentId(String value) {

    public PaymentId {
        Objects.requireNonNull(value, "PaymentId must not be null");
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }
}
