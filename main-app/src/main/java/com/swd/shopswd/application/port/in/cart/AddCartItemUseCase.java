package com.swd.shopswd.application.port.in.cart;

public interface AddCartItemUseCase {

    CartResult addItem(AddCartItemCommand command);
}
