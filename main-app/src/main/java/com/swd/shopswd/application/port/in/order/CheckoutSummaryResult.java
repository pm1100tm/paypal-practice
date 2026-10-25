package com.swd.shopswd.application.port.in.order;

import com.swd.shopswd.domain.shared.value.Money;
import java.util.List;

/**
 * 체크아웃 페이지 금액 미리보기.
 * 실제 결제 금액은 주문 생성 시점에 서버가 다시 계산한다.
 */
public record CheckoutSummaryResult(
    String cartId,
    List<OrderLineResult> items,
    Money itemTotal,
    Money shippingCost,
    Money totalAmount,
    Money freeShippingThreshold
) {}
