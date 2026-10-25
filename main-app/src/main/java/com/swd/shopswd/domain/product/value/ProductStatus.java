package com.swd.shopswd.domain.product.value;

public enum ProductStatus {
    ON_SALE,
    SOLD_OUT,
    DISCONTINUED;

    public boolean isPurchasable() {
        return this == ON_SALE;
    }
}
