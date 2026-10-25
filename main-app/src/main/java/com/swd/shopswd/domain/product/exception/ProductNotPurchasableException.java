package com.swd.shopswd.domain.product.exception;

import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.exception.DomainException;

public class ProductNotPurchasableException extends DomainException {

    public ProductNotPurchasableException(ProductId id) {
        super("PRODUCT_NOT_PURCHASABLE", "Product is not available for purchase: " + id.value());
    }
}
