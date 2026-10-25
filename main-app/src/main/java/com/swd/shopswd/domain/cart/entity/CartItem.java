package com.swd.shopswd.domain.cart.entity;

import com.swd.shopswd.domain.cart.exception.InvalidCartItemQuantityException;
import com.swd.shopswd.domain.product.value.ProductId;
import java.util.Objects;

/**
 * 장바구니 라인 아이템 (Cart Aggregate 내부 Entity).
 * 가격은 저장하지 않는다 — 조회/주문 시점의 상품 가격을 항상 사용한다.
 * 수량 변경은 Cart Aggregate 를 통해서만 수행한다.
 */
public class CartItem {

    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 99;

    private final ProductId productId;
    private int quantity;

    private CartItem(ProductId productId, int quantity) {
        this.productId = Objects.requireNonNull(productId);
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public static CartItem of(ProductId productId, int quantity) {
        return new CartItem(productId, quantity);
    }

    public void changeQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public void increase(int amount) {
        changeQuantity(this.quantity + amount);
    }

    private static void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new InvalidCartItemQuantityException(quantity, MIN_QUANTITY, MAX_QUANTITY);
        }
    }

    public ProductId getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}
