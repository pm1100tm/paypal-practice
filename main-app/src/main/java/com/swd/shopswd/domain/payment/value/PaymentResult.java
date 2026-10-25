package com.swd.shopswd.domain.payment.value;

import java.util.Objects;

/**
 * PG 결제 확정(캡처) 결과.
 */
public sealed interface PaymentResult {

    record Success(TransactionInfo transactionInfo) implements PaymentResult {
        public Success {
            Objects.requireNonNull(transactionInfo);
        }
    }

    /** PG 거절 — 고객 결제수단 문제 (재시도 의미 없음) */
    record Declined(String code, String message) implements PaymentResult {}

    record Failed(String code, String message, PaymentErrorType errorType) implements PaymentResult {
        public Failed {
            Objects.requireNonNull(errorType);
        }
    }

    static PaymentResult success(TransactionInfo transactionInfo) {
        return new Success(transactionInfo);
    }

    static PaymentResult declined(String code, String message) {
        return new Declined(code, message);
    }

    static PaymentResult failure(String code, String message, PaymentErrorType errorType) {
        return new Failed(code, message, errorType);
    }
}
