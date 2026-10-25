package com.swd.shopswd.domain.order.repository;

import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.value.OrderId;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    Order save(Order order);
}
