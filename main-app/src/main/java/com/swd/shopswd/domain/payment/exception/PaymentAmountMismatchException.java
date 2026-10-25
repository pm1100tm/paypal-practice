package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.shared.exception.DomainException;
import com.swd.shopswd.domain.shared.value.Money;

public class PaymentAmountMismatchException extends DomainException {

    public PaymentAmountMismatchException(OrderId orderId, Money expected, Money actual) {
        super("PAYMENT_AMOUNT_MISMATCH",
            "Captured amount does not match order " + orderId.value()
                + ": expected=" + expected + ", actual=" + actual);
    }
}
