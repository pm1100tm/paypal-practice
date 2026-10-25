package com.swd.shopswd.domain.cart.exception;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;

public class CartNotFoundException extends ResourceNotFoundException {

    public CartNotFoundException(CartId id) {
        super("CART_NOT_FOUND", "Cart not found: " + id.value());
    }
}
