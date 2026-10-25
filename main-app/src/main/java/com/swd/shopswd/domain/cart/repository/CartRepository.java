package com.swd.shopswd.domain.cart.repository;

import com.swd.shopswd.domain.cart.aggregate.Cart;
import com.swd.shopswd.domain.cart.value.CartId;
import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findById(CartId id);

    Cart save(Cart cart);
}
