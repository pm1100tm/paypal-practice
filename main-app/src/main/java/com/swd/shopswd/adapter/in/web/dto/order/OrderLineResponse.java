package com.swd.shopswd.adapter.in.web.dto.order;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.order.OrderLineResult;

public record OrderLineResponse(
    Long productId,
    String name,
    MoneyResponse unitPrice,
    int quantity,
    MoneyResponse lineTotal
) {
    public static OrderLineResponse from(OrderLineResult result) {
        return new OrderLineResponse(
            result.productId(),
            result.name(),
            MoneyResponse.from(result.unitPrice()),
            result.quantity(),
            MoneyResponse.from(result.lineTotal())
        );
    }
}
