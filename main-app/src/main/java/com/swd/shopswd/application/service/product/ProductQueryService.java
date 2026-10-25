package com.swd.shopswd.application.service.product;

import com.swd.shopswd.application.port.in.product.GetProductUseCase;
import com.swd.shopswd.application.port.in.product.ProductResult;
import com.swd.shopswd.domain.product.exception.ProductNotFoundException;
import com.swd.shopswd.domain.product.repository.ProductRepository;
import com.swd.shopswd.domain.product.value.ProductId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService implements GetProductUseCase {

    private final ProductRepository productRepository;

    @Override
    public List<ProductResult> getProducts() {
        return productRepository.findAll().stream()
            .map(ProductResult::from)
            .toList();
    }

    @Override
    public ProductResult getProduct(Long productId) {
        ProductId id = ProductId.of(productId);
        return productRepository.findById(id)
            .map(ProductResult::from)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
