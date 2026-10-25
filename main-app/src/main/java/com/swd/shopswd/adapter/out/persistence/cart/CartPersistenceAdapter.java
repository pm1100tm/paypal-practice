package com.swd.shopswd.adapter.out.persistence.cart;

import com.swd.shopswd.domain.cart.aggregate.Cart;
import com.swd.shopswd.domain.cart.entity.CartItem;
import com.swd.shopswd.domain.cart.repository.CartRepository;
import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.product.value.ProductId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartPersistenceAdapter implements CartRepository {

    private final CartJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findById(CartId id) {
        return jpaRepository.findById(id.value()).map(CartPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        CartJpaEntity entity = jpaRepository.findById(cart.getId().value())
            .orElseGet(() -> new CartJpaEntity(cart.getId().value(), cart.getCreatedAt()));

        List<CartItemEmbeddable> items = cart.getItems().stream()
            .map(item -> new CartItemEmbeddable(item.getProductId().value(), item.getQuantity()))
            .toList();
        entity.update(items, cart.getUpdatedAt());

        return toDomain(jpaRepository.save(entity));
    }

    private static Cart toDomain(CartJpaEntity entity) {
        List<CartItem> items = entity.getItems().stream()
            .map(item -> CartItem.of(ProductId.of(item.getProductId()), item.getQuantity()))
            .toList();
        return Cart.restore(CartId.of(entity.getId()), items, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
