package com.swd.shopswd.domain.cart.aggregate;

import com.swd.shopswd.domain.cart.entity.CartItem;
import com.swd.shopswd.domain.cart.exception.CartItemNotFoundException;
import com.swd.shopswd.domain.cart.exception.CartItemLimitExceededException;
import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.product.value.ProductId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cart Aggregate Root (비회원 장바구니).
 *
 * 불변식:
 * - 동일 상품은 하나의 라인으로 합쳐진다.
 * - 라인 수량은 {@link CartItem#MIN_QUANTITY} ~ {@link CartItem#MAX_QUANTITY}.
 * - 라인 수는 {@link #MAX_LINES} 이하.
 */
public class Cart {

    public static final int MAX_LINES = 50;

    private final CartId id;
    private final Map<ProductId, CartItem> items;
    private final Instant createdAt;
    private Instant updatedAt;

    private Cart(CartId id, List<CartItem> items, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.items = new LinkedHashMap<>();
        items.forEach(item -> this.items.put(item.getProductId(), item));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Cart create(Instant now) {
        return new Cart(CartId.generate(), List.of(), now, now);
    }

    public static Cart restore(CartId id, List<CartItem> items, Instant createdAt, Instant updatedAt) {
        return new Cart(id, items, createdAt, updatedAt);
    }

    /** 상품을 담는다. 이미 담긴 상품이면 수량을 더한다. */
    public void addItem(ProductId productId, int quantity, Instant now) {
        CartItem existing = items.get(productId);
        if (existing != null) {
            existing.increase(quantity);
        } else {
            if (items.size() >= MAX_LINES) {
                throw new CartItemLimitExceededException(MAX_LINES);
            }
            items.put(productId, CartItem.of(productId, quantity));
        }
        touch(now);
    }

    public void changeQuantity(ProductId productId, int quantity, Instant now) {
        findItem(productId).changeQuantity(quantity);
        touch(now);
    }

    public void removeItem(ProductId productId, Instant now) {
        if (items.remove(productId) == null) {
            throw new CartItemNotFoundException(id, productId);
        }
        touch(now);
    }

    /** 결제 완료 후 장바구니 비우기 */
    public void clear(Instant now) {
        items.clear();
        touch(now);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return List.copyOf(new ArrayList<>(items.values()));
    }

    public List<ProductId> productIds() {
        return List.copyOf(items.keySet());
    }

    private CartItem findItem(ProductId productId) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new CartItemNotFoundException(id, productId);
        }
        return item;
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now);
    }

    public CartId getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
