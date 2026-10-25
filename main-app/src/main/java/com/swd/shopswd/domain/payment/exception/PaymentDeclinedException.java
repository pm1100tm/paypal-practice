package com.swd.shopswd.domain.payment.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

/**
 * PG 거절 (결제수단 한도 초과, 잔액 부족 등).
 */
public class PaymentDeclinedException extends DomainException {

    public PaymentDeclinedException(String pgCode) {
        super("PAYMENT_DECLINED", "The payment was declined by PayPal" + (pgCode != null ? " (" + pgCode + ")" : ""));
    }
}
