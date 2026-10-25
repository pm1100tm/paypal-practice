package com.swd.shopswd.adapter.out.persistence.cart;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, String> {
}
