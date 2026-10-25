package com.swd.shopswd.adapter.in.web.dto.payment;

import com.swd.shopswd.application.port.in.payment.CreatePaymentResult;

/**
 * @param paymentId PayPal Order ID — PayPal JS SDK createOrder 콜백에서 그대로 반환해야 한다.
 */
public record CreatePaymentResponse(
    String orderId,
    String paymentId,
    String paymentMethod,
    String initiationType,
    String redirectUrl
) {
    public static CreatePaymentResponse from(CreatePaymentResult result) {
        return new CreatePaymentResponse(
            result.orderId(),
            result.paymentId(),
            result.paymentMethod(),
            result.initiationType(),
            result.approveUrl()
        );
    }
}
