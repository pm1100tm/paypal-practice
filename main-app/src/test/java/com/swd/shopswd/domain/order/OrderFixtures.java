package com.swd.shopswd.domain.order;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.order.value.OrderContact;
import com.swd.shopswd.domain.order.value.ShippingAddress;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Instant;
import java.util.List;

public final class OrderFixtures {

    public static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private OrderFixtures() {}

    public static ShippingAddress address() {
        return new ShippingAddress("Jane Doe", "1 Main St", null, "San Jose", "CA", "95131", "us");
    }

    /** 18.50 x 2 + 15.90 x 1 = 52.90, shipping 5.00 → total 57.90 */
    public static Order pendingOrder() {
        return Order.place(
            CartId.generate(),
            List.of(
                new OrderItem(ProductId.of(1001L), "Toner", Money.usd("18.50"), 2),
                new OrderItem(ProductId.of(1003L), "Sunscreen", Money.usd("15.90"), 1)
            ),
            Money.usd("5.00"),
            new OrderContact("jane@example.com", null),
            address(),
            NOW
        );
    }
}
