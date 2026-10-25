package com.swd.shopswd.domain.product.aggregate;

import com.swd.shopswd.domain.product.exception.ProductNotPurchasableException;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.product.value.ProductStatus;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.Objects;

/**
 * Product Aggregate Root.
 * v1 은 상품 조회 전용이며, 구매 가능 여부와 판매가만 판단한다.
 */
public class Product {

    private final ProductId id;
    private final String name;
    private final String brand;
    private final Money price;
    private final String imageUrl;
    private final ProductStatus status;

    private Product(ProductId id, String name, String brand, Money price, String imageUrl, ProductStatus status) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.brand = brand;
        this.price = Objects.requireNonNull(price);
        this.imageUrl = imageUrl;
        this.status = Objects.requireNonNull(status);
    }

    public static Product restore(ProductId id, String name, String brand, Money price, String imageUrl,
                                  ProductStatus status) {
        return new Product(id, name, brand, price, imageUrl, status);
    }

    public boolean isPurchasable() {
        return status.isPurchasable();
    }

    public void ensurePurchasable() {
        if (!isPurchasable()) {
            throw new ProductNotPurchasableException(id);
        }
    }

    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public Money getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public ProductStatus getStatus() { return status; }
}
