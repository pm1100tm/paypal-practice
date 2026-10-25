package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(OrderId orderId) {
        super("PAYMENT_NOT_FOUND", "Payment not found for order: " + orderId.value());
    }
}
