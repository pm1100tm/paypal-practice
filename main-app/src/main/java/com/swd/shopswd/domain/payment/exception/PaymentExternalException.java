package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

/**
 * PG 통신 실패 (4xx 요청 거부, 5xx, 네트워크 장애, 응답 누락 등).
 */
public class PaymentExternalException extends DomainException {

    public PaymentExternalException(String code, String message) {
        super(code, message);
    }

    public PaymentExternalException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
