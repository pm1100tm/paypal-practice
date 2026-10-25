package com.swd.shopswd.adapter.in.web.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * @param paymentMethod v1 은 PAYPAL 만 허용
 */
public record CreatePaymentRequest(
    @NotBlank String orderId,
    @NotBlank @Pattern(regexp = "PAYPAL", message = "only PAYPAL is supported") String paymentMethod
) {}
