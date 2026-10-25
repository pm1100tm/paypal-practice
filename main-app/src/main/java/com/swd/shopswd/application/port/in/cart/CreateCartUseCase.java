package com.swd.shopswd.application.port.in.cart;

public interface CreateCartUseCase {

    /** 비회원 장바구니를 새로 발급한다. */
    CartResult createCart();
}
