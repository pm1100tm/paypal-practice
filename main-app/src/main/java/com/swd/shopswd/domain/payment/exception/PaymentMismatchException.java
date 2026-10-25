package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.shared.exception.DomainException;

public class PaymentMismatchException extends DomainException {

    public PaymentMismatchException(OrderId orderId) {
        super("PAYMENT_MISMATCH", "Payment reference does not belong to order: " + orderId.value());
    }
}
