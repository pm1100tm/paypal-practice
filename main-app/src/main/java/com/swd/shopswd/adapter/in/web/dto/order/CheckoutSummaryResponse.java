package com.swd.shopswd.adapter.in.web.dto.order;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.order.CheckoutSummaryResult;
import java.util.List;

public record CheckoutSummaryResponse(
    String cartId,
    List<OrderLineResponse> items,
    MoneyResponse itemTotal,
    MoneyResponse shippingCost,
    MoneyResponse totalAmount,
    MoneyResponse freeShippingThreshold
) {
    public static CheckoutSummaryResponse from(CheckoutSummaryResult result) {
        return new CheckoutSummaryResponse(
            result.cartId(),
            result.items().stream().map(OrderLineResponse::from).toList(),
            MoneyResponse.from(result.itemTotal()),
            MoneyResponse.from(result.shippingCost()),
            MoneyResponse.from(result.totalAmount()),
            MoneyResponse.from(result.freeShippingThreshold())
        );
    }
}
