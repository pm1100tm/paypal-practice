package com.swd.shopswd.application.port.in.product;

import com.swd.shopswd.domain.product.aggregate.Product;
import com.swd.shopswd.domain.shared.value.Money;

public record ProductResult(
    Long productId,
    String name,
    String brand,
    Money price,
    String imageUrl,
    boolean purchasable
) {
    public static ProductResult from(Product product) {
        return new ProductResult(
            product.getId().value(),
            product.getName(),
            product.getBrand(),
            product.getPrice(),
            product.getImageUrl(),
            product.isPurchasable()
        );
    }
}
