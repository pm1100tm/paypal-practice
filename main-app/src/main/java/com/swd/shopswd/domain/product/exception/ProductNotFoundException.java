package com.swd.shopswd.domain.product.exception;

import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(ProductId id) {
        super("PRODUCT_NOT_FOUND", "Product not found: " + id.value());
    }
}
