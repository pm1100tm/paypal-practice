package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

public class PaymentFailedException extends DomainException {

    public PaymentFailedException(String code, String message) {
        super(code, message);
    }
}
