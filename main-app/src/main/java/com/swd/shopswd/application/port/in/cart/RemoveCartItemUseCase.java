package com.swd.shopswd.application.port.in.cart;

public interface RemoveCartItemUseCase {

    CartResult removeItem(String cartId, Long productId);
}
