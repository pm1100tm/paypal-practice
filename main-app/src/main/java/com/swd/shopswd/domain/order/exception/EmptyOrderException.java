package com.swd.shopswd.domain.order.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

public class EmptyOrderException extends DomainException {

    public EmptyOrderException() {
        super("EMPTY_ORDER", "Cannot place an order without items");
    }
}
