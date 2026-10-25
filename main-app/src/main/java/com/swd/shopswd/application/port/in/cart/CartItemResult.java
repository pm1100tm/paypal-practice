package com.swd.shopswd.application.port.in.cart;

import com.swd.shopswd.domain.shared.value.Money;

public record CartItemResult(
    Long productId,
    String name,
    String brand,
    String imageUrl,
    Money unitPrice,
    int quantity,
    Money lineTotal,
    boolean purchasable
) {}
