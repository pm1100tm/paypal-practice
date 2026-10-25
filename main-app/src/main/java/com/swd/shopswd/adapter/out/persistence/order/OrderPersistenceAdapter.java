package com.swd.shopswd.adapter.out.persistence.order;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.order.repository.OrderRepository;
import com.swd.shopswd.domain.order.value.OrderContact;
import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.order.value.ShippingAddress;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(OrderPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = jpaRepository.findById(order.getId().value())
            .orElseGet(() -> new OrderJpaEntity(order.getId().value()));

        entity.setCartId(order.getCartId().value());
        entity.getItems().clear();
        entity.getItems().addAll(order.getItems().stream()
            .map(item -> new OrderItemEmbeddable(item.productId().value(), item.productName(),
                item.unitPrice().amount(), item.quantity()))
            .toList());
        entity.setCurrency(order.getTotalAmount().currencyCode());
        entity.setItemTotal(order.getItemTotal().amount());
        entity.setShippingCost(order.getShippingCost().amount());
        entity.setTotalAmount(order.getTotalAmount().amount());

        entity.setEmail(order.getContact().email());
        entity.setPhone(order.getContact().phone());

        ShippingAddress address = order.getShippingAddress();
        entity.setShipFullName(address.fullName());
        entity.setShipAddressLine1(address.addressLine1());
        entity.setShipAddressLine2(address.addressLine2());
        entity.setShipCity(address.city());
        entity.setShipState(address.state());
        entity.setShipPostalCode(address.postalCode());
        entity.setShipCountryCode(address.countryCode());

        entity.setStatus(order.getStatus());
        entity.setCancelReason(order.getCancelReason());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setPaidAt(order.getPaidAt());
        entity.setCancelledAt(order.getCancelledAt());

        return toDomain(jpaRepository.save(entity));
    }

    private static Order toDomain(OrderJpaEntity entity) {
        String currency = entity.getCurrency();
        List<OrderItem> items = entity.getItems().stream()
            .map(item -> new OrderItem(ProductId.of(item.getProductId()), item.getProductName(),
                Money.of(item.getUnitPrice(), currency), item.getQuantity()))
            .toList();

        return Order.restore(
            OrderId.of(entity.getId()),
            CartId.of(entity.getCartId()),
            items,
            Money.of(entity.getItemTotal(), currency),
            Money.of(entity.getShippingCost(), currency),
            Money.of(entity.getTotalAmount(), currency),
            new OrderContact(entity.getEmail(), entity.getPhone()),
            new ShippingAddress(
                entity.getShipFullName(),
                entity.getShipAddressLine1(),
                entity.getShipAddressLine2(),
                entity.getShipCity(),
                entity.getShipState(),
                entity.getShipPostalCode(),
                entity.getShipCountryCode()
            ),
            entity.getStatus(),
            entity.getCancelReason(),
            entity.getCreatedAt(),
            entity.getPaidAt(),
            entity.getCancelledAt()
        );
    }
}
