package com.swd.shopswd.application.port.in.payment;

public interface CreatePaymentUseCase {

    /** 주문 금액으로 PG(PayPal) 주문을 생성한다. */
    CreatePaymentResult createPayment(CreatePaymentCommand command);
}
