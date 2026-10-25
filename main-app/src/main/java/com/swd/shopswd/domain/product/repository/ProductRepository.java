package com.swd.shopswd.domain.product.repository;

import com.swd.shopswd.domain.product.aggregate.Product;
import com.swd.shopswd.domain.product.value.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(ProductId id);

    Map<ProductId, Product> findAllByIds(Collection<ProductId> ids);

    List<Product> findAll();
}
