package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.payment.value.PaymentId;
import com.swd.shopswd.domain.payment.value.PaymentStatus;
import com.swd.shopswd.domain.shared.exception.DomainException;

public class InvalidPaymentStatusException extends DomainException {

    public InvalidPaymentStatusException(PaymentId id, PaymentStatus current, String action) {
        super("INVALID_PAYMENT_STATUS",
            "Cannot " + action + " payment " + id.value() + " in status " + current);
    }
}
