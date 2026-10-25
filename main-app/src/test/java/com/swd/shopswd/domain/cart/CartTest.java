package com.swd.shopswd.domain.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.swd.shopswd.domain.cart.aggregate.Cart;
import com.swd.shopswd.domain.cart.exception.CartItemNotFoundException;
import com.swd.shopswd.domain.cart.exception.InvalidCartItemQuantityException;
import com.swd.shopswd.domain.product.value.ProductId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CartTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final ProductId P1 = ProductId.of(1L);
    private static final ProductId P2 = ProductId.of(2L);

    @Test
    void 같은_상품을_담으면_수량이_합쳐진다() {
        Cart cart = Cart.create(NOW);
        cart.addItem(P1, 1, NOW);
        cart.addItem(P1, 2, NOW);
        cart.addItem(P2, 1, NOW);

        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(3);
    }

    @Test
    void 수량_범위를_벗어나면_예외() {
        Cart cart = Cart.create(NOW);
        assertThatThrownBy(() -> cart.addItem(P1, 0, NOW)).isInstanceOf(InvalidCartItemQuantityException.class);

        cart.addItem(P1, 99, NOW);
        assertThatThrownBy(() -> cart.addItem(P1, 1, NOW)).isInstanceOf(InvalidCartItemQuantityException.class);
    }

    @Test
    void 없는_상품은_수정_삭제할_수_없다() {
        Cart cart = Cart.create(NOW);
        assertThatThrownBy(() -> cart.changeQuantity(P1, 2, NOW)).isInstanceOf(CartItemNotFoundException.class);
        assertThatThrownBy(() -> cart.removeItem(P1, NOW)).isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void 비우기() {
        Cart cart = Cart.create(NOW);
        cart.addItem(P1, 1, NOW);
        cart.clear(NOW);
        assertThat(cart.isEmpty()).isTrue();
    }
}
