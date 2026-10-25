package com.swd.shopswd.adapter.in.web.dto.product;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.product.ProductResult;

public record ProductResponse(
    Long productId,
    String name,
    String brand,
    MoneyResponse price,
    String imageUrl,
    boolean purchasable
) {
    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(
            result.productId(),
            result.name(),
            result.brand(),
            MoneyResponse.from(result.price()),
            result.imageUrl(),
            result.purchasable()
        );
    }
}
