package com.swd.shopswd.domain.order.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(OrderId id) {
        super("ORDER_NOT_FOUND", "Order not found: " + id.value());
    }
}
