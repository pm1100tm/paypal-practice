package com.swd.shopswd.adapter.in.web.dto.order;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.order.OrderResult;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    String orderId,
    String status,
    List<OrderLineResponse> items,
    MoneyResponse itemTotal,
    MoneyResponse shippingCost,
    MoneyResponse totalAmount,
    String recipientName,
    String countryCode,
    Instant createdAt,
    Instant paidAt
) {
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
            result.orderId(),
            result.status().name(),
            result.items().stream().map(OrderLineResponse::from).toList(),
            MoneyResponse.from(result.itemTotal()),
            MoneyResponse.from(result.shippingCost()),
            MoneyResponse.from(result.totalAmount()),
            result.recipientName(),
            result.countryCode(),
            result.createdAt(),
            result.paidAt()
        );
    }
}
