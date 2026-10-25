package com.swd.shopswd.application.port.in.payment;

import com.swd.shopswd.domain.order.value.OrderStatus;

public record CompletePaymentResult(String orderId, OrderStatus status) {}
