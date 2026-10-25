package com.swd.shopswd.application.port.out.payment;

import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.exception.PaymentExternalException;
import com.swd.shopswd.domain.payment.value.PaymentInitiation;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentResult;

/**
 * PG 연동 Outbound Port.
 */
public interface PaymentGateway {

    PaymentMethod method();

    /**
     * PG 주문 생성.
     * @throws PaymentExternalException PG 요청 거부/오류/네트워크 장애
     */
    PaymentInitiation initiate(Payment payment, Order order);

    /**
     * 구매자 승인 이후 결제 확정(캡처).
     * PG 의 거절/검증 실패는 {@link PaymentResult} 로 반환한다.
     * @throws PaymentExternalException 캡처 결과를 알 수 없는 통신 장애(5xx/네트워크)
     */
    PaymentResult capture(Payment payment);
}
