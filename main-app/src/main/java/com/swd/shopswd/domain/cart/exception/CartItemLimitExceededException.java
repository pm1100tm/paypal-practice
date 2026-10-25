package com.swd.shopswd.domain.cart.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

public class CartItemLimitExceededException extends DomainException {

    public CartItemLimitExceededException(int maxLines) {
        super("CART_ITEM_LIMIT_EXCEEDED", "A cart can hold up to " + maxLines + " products");
    }
}
