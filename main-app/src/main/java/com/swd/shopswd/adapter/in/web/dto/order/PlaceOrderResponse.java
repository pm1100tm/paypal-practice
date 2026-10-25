package com.swd.shopswd.adapter.in.web.dto.order;

import com.swd.shopswd.adapter.in.web.dto.shared.MoneyResponse;
import com.swd.shopswd.application.port.in.order.PlaceOrderResult;

public record PlaceOrderResponse(String orderId, MoneyResponse totalAmount) {

    public static PlaceOrderResponse from(PlaceOrderResult result) {
        return new PlaceOrderResponse(result.orderId(), MoneyResponse.from(result.totalAmount()));
    }
}
