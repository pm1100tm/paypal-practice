package com.swd.shopswd.application.port.in.payment;

public interface CompletePaymentUseCase {

    /** 구매자 승인 이후 PG 캡처를 수행하고 주문을 결제 완료 처리한다. */
    CompletePaymentResult completePayment(CompletePaymentCommand command);
}
