package com.swd.shopswd.domain.cart.exception;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;

public class CartItemNotFoundException extends ResourceNotFoundException {

    public CartItemNotFoundException(CartId cartId, ProductId productId) {
        super("CART_ITEM_NOT_FOUND",
            "Product " + productId.value() + " is not in cart " + cartId.value());
    }
}
