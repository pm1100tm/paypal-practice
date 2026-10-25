package com.swd.shopswd.domain.order.exception;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.order.value.OrderStatus;
import com.swd.shopswd.domain.shared.exception.DomainException;

public class InvalidOrderStatusException extends DomainException {

    public InvalidOrderStatusException(OrderId id, OrderStatus current, String action) {
        super("INVALID_ORDER_STATUS",
            "Cannot " + action + " for order " + id.value() + " in status " + current);
    }
}
