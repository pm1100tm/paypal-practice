package com.swd.shopswd.application.port.in.order;

public interface PlaceOrderUseCase {

    /** 장바구니 기준으로 결제 대기 주문을 생성한다. */
    PlaceOrderResult placeOrder(PlaceOrderCommand command);
}
