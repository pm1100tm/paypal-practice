package com.swd.shopswd.adapter.in.web.dto.payment;

import com.swd.shopswd.application.port.in.payment.CompletePaymentResult;

public record CapturePaymentResponse(String orderId, String status) {

    public static CapturePaymentResponse from(CompletePaymentResult result) {
        return new CapturePaymentResponse(result.orderId(), result.status().name());
    }
}
