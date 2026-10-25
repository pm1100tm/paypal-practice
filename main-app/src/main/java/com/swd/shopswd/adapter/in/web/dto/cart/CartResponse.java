package com.swd.shopswd.adapter.in.web.dto.cart;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.cart.CartItemResult;
import com.swd.shopswd.application.port.in.cart.CartResult;
import java.util.List;

public record CartResponse(
    String cartId,
    List<Item> items,
    int totalQuantity,
    MoneyResponse subtotal,
    boolean checkoutAvailable
) {

    public record Item(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        MoneyResponse unitPrice,
        int quantity,
        MoneyResponse lineTotal,
        boolean purchasable
    ) {
        static Item from(CartItemResult result) {
            return new Item(
                result.productId(),
                result.name(),
                result.brand(),
                result.imageUrl(),
                MoneyResponse.from(result.unitPrice()),
                result.quantity(),
                MoneyResponse.from(result.lineTotal()),
                result.purchasable()
            );
        }
    }

    public static CartResponse from(CartResult result) {
        return new CartResponse(
            result.cartId(),
            result.items().stream().map(Item::from).toList(),
            result.totalQuantity(),
            MoneyResponse.from(result.subtotal()),
            result.checkoutAvailable()
        );
    }
}
