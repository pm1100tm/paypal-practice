package com.swd.shopswd.adapter.out.persistence.product;

import com.swd.shopswd.domain.product.aggregate.Product;
import com.swd.shopswd.domain.product.repository.ProductRepository;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public Map<ProductId, Product> findAllByIds(Collection<ProductId> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Long> rawIds = ids.stream().map(ProductId::value).toList();
        return jpaRepository.findAllById(rawIds).stream()
            .map(ProductPersistenceAdapter::toDomain)
            .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll(Sort.by("id")).stream()
            .map(ProductPersistenceAdapter::toDomain)
            .toList();
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(
            ProductId.of(entity.getId()),
            entity.getName(),
            entity.getBrand(),
            Money.of(entity.getPrice(), entity.getCurrency()),
            entity.getImageUrl(),
            entity.getStatus()
        );
    }
}
