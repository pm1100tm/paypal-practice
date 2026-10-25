package com.swd.shopswd.domain.payment.repository;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import java.util.Optional;

public interface PaymentRepository {

    /** v1 정책: 주문당 결제는 1건 */
    Optional<Payment> findByOrderId(OrderId orderId);

    Payment save(Payment payment);
}
