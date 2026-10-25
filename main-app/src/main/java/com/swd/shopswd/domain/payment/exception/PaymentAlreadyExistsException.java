package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.shared.exception.DomainException;

public class PaymentAlreadyExistsException extends DomainException {

    public PaymentAlreadyExistsException(OrderId orderId) {
        super("PAYMENT_ALREADY_EXISTS", "Payment already exists for order: " + orderId.value());
    }
}
