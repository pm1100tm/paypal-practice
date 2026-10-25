package com.swd.shopswd.domain.product.value;

import java.util.Objects;

public record ProductId(Long value) {

    public ProductId {
        Objects.requireNonNull(value, "ProductId must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("ProductId must be positive: " + value);
        }
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }
}
