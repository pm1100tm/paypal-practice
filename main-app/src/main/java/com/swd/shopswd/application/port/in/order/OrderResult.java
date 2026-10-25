package com.swd.shopswd.application.port.in.order;

import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.value.OrderStatus;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Instant;
import java.util.List;

/**
 * 주문 조회 결과 (주문 완료 페이지용).
 * 비회원 조회이므로 이메일/상세 주소 등 개인정보는 노출하지 않는다.
 */
public record OrderResult(
    String orderId,
    OrderStatus status,
    List<OrderLineResult> items,
    Money itemTotal,
    Money shippingCost,
    Money totalAmount,
    String recipientName,
    String countryCode,
    Instant createdAt,
    Instant paidAt
) {
    public static OrderResult from(Order order) {
        return new OrderResult(
            order.getId().value(),
            order.getStatus(),
            order.getItems().stream().map(OrderLineResult::from).toList(),
            order.getItemTotal(),
            order.getShippingCost(),
            order.getTotalAmount(),
            order.getShippingAddress().fullName(),
            order.getShippingAddress().countryCode(),
            order.getCreatedAt(),
            order.getPaidAt()
        );
    }
}
