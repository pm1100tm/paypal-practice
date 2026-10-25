package com.swd.shopswd.adapter.in.web.api;

import com.swd.shopswd.adapter.in.web.dto.product.ProductResponse;
import com.swd.shopswd.application.port.in.product.GetProductUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product Controller (조회 전용)
 */
@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductController {

    private final GetProductUseCase getProductUseCase;

    @GetMapping
    public List<ProductResponse> getProducts() {
        return getProductUseCase.getProducts().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return ProductResponse.from(getProductUseCase.getProduct(productId));
    }
}
