package com.swd.shopswd.application.port.in.cart;

public interface ChangeCartItemQuantityUseCase {

    CartResult changeQuantity(ChangeCartItemQuantityCommand command);
}
