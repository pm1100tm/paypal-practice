package com.swd.shopswd.domain.cart.exception;

import com.swd.shopswd.domain.shared.exception.DomainException;

public class InvalidCartItemQuantityException extends DomainException {

    public InvalidCartItemQuantityException(int quantity, int min, int max) {
        super("INVALID_CART_ITEM_QUANTITY",
            "Quantity must be between " + min + " and " + max + " but was " + quantity);
    }
}
