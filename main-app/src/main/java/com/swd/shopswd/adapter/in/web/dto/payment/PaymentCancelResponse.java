package com.swd.shopswd.adapter.in.web.dto.payment;

public record PaymentCancelResponse(boolean success, String msg) {

    public static PaymentCancelResponse ok() {
        return new PaymentCancelResponse(true, "OK");
    }

    public static PaymentCancelResponse fail(String msg) {
        return new PaymentCancelResponse(false, msg);
    }
}
