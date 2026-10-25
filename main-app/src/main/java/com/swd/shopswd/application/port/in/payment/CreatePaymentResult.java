package com.swd.shopswd.application.port.in.payment;

/**
 * @param paymentId      PG 주문 ID (PayPal Order ID) — PayPal JS SDK createOrder 콜백의 반환값
 * @param initiationType 결제 진행 방식. PayPal 은 항상 CLIENT_SDK
 * @param approveUrl     PayPal 승인 페이지 URL (SDK 미사용 시 참고용)
 */
public record CreatePaymentResult(
    String orderId,
    String paymentId,
    String paymentMethod,
    String initiationType,
    String approveUrl
) {}
