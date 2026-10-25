package com.swd.shopswd.application.port.in.order;

import com.swd.shopswd.domain.shared.value.Money;

public record PlaceOrderResult(String orderId, Money totalAmount) {}
