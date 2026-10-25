package com.swd.shopswd.application.port.in.payment;

/**
 * @param paymentId PayPal Order ID (createPayment 응답의 paymentId)
 */
public record CompletePaymentCommand(String orderId, String paymentId) {}
