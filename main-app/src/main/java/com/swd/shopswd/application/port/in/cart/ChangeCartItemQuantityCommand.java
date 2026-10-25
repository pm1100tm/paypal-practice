package com.swd.shopswd.application.port.in.cart;

public record ChangeCartItemQuantityCommand(String cartId, Long productId, int quantity) {}
