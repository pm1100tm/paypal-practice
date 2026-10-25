package com.swd.shopswd.application.port.in.order;

public interface GetOrderUseCase {

    OrderResult getOrder(String orderId);
}
