package com.swd.shopswd.domain.order.entity;

import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.Objects;

/**
 * 주문 라인 — 주문 시점의 상품명/단가 스냅샷.
 */
public record OrderItem(ProductId productId, String productName, Money unitPrice, int quantity) {

    public OrderItem {
        Objects.requireNonNull(productId);
        Objects.requireNonNull(productName);
        Objects.requireNonNull(unitPrice);
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }
}
