package com.swd.shopswd.adapter.in.web.api;

import com.swd.shopswd.adapter.in.web.dto.order.CheckoutPreviewRequest;
import com.swd.shopswd.adapter.in.web.dto.order.CheckoutSummaryResponse;
import com.swd.shopswd.adapter.in.web.dto.order.OrderResponse;
import com.swd.shopswd.adapter.in.web.dto.order.PlaceOrderRequest;
import com.swd.shopswd.adapter.in.web.dto.order.PlaceOrderResponse;
import com.swd.shopswd.application.port.in.order.GetOrderUseCase;
import com.swd.shopswd.application.port.in.order.PlaceOrderUseCase;
import com.swd.shopswd.application.port.in.order.PreviewCheckoutUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 비회원 Order Controller.
 */
@RestController
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PreviewCheckoutUseCase previewCheckoutUseCase;
    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    /** 체크아웃 페이지 금액 미리보기 (상품 합계 / 배송비 / 총액) */
    @PostMapping("/checkout")
    public CheckoutSummaryResponse previewCheckout(@Valid @RequestBody CheckoutPreviewRequest request) {
        return CheckoutSummaryResponse.from(previewCheckoutUseCase.preview(request.cartId()));
    }

    /** 결제 대기 주문 생성 (PayPal 버튼 createOrder 콜백에서 호출) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return PlaceOrderResponse.from(placeOrderUseCase.placeOrder(request.toCommand()));
    }

    /** 주문 완료 페이지 조회 */
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return OrderResponse.from(getOrderUseCase.getOrder(orderId));
    }
}
