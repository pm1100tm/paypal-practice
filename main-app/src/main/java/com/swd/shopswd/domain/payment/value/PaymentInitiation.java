package com.swd.shopswd.domain.payment.value;

import java.util.Objects;

/**
 * PG 결제 시작 결과.
 * PayPal JS SDK 는 pgOrderId 만 있으면 팝업을 띄울 수 있다 (approveUrl 은 참고용).
 */
public record PaymentInitiation(String pgOrderId, String approveUrl) {

    public PaymentInitiation {
        Objects.requireNonNull(pgOrderId, "pgOrderId must not be null");
    }
}
