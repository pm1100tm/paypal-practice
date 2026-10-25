package com.swd.shopswd.application.port.in.product;

import java.util.List;

public interface GetProductUseCase {

    List<ProductResult> getProducts();

    ProductResult getProduct(Long productId);
}
