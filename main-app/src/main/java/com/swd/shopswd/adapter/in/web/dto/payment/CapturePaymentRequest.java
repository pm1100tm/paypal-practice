package com.swd.shopswd.adapter.in.web.dto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * @param paymentId PayPal Order ID (createPayment 응답의 paymentId)
 */
public record CapturePaymentRequest(
    @NotBlank String orderId,
    @NotBlank String paymentId
) {}
