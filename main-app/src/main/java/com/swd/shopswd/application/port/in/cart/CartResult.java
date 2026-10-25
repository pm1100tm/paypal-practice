package com.swd.shopswd.application.port.in.cart;

import com.swd.shopswd.domain.shared.value.Money;
import java.util.List;

/**
 * @param subtotal          구매 가능한 상품만의 합계
 * @param checkoutAvailable 장바구니가 비어있지 않고 모든 상품이 구매 가능할 때 true
 */
public record CartResult(
    String cartId,
    List<CartItemResult> items,
    int totalQuantity,
    Money subtotal,
    boolean checkoutAvailable
) {}
