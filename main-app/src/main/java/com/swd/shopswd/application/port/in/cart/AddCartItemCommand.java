package com.swd.shopswd.application.port.in.cart;

public record AddCartItemCommand(String cartId, Long productId, int quantity) {}
