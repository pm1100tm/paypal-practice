package com.swd.shopswd.application.port.in.order;

import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.shared.value.Money;

public record OrderLineResult(Long productId, String name, Money unitPrice, int quantity, Money lineTotal) {

    public static OrderLineResult from(OrderItem item) {
        return new OrderLineResult(item.productId().value(), item.productName(), item.unitPrice(),
            item.quantity(), item.lineTotal());
    }
}
