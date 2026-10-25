package com.swd.shopswd.application.service.order;

import com.swd.shopswd.application.port.in.order.GetOrderUseCase;
import com.swd.shopswd.application.port.in.order.OrderResult;
import com.swd.shopswd.domain.order.exception.OrderNotFoundException;
import com.swd.shopswd.domain.order.repository.OrderRepository;
import com.swd.shopswd.domain.order.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public OrderResult getOrder(String orderId) {
        OrderId id = OrderId.of(orderId);
        return orderRepository.findById(id)
            .map(OrderResult::from)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
