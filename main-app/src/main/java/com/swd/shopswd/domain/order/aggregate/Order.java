package com.swd.shopswd.domain.order.aggregate;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.order.exception.EmptyOrderException;
import com.swd.shopswd.domain.order.exception.InvalidOrderStatusException;
import com.swd.shopswd.domain.order.value.CancelReason;
import com.swd.shopswd.domain.order.value.OrderContact;
import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.order.value.OrderStatus;
import com.swd.shopswd.domain.order.value.ShippingAddress;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Order Aggregate Root (비회원 주문).
 *
 * 상태 전이:
 * <pre>
 * PENDING_PAYMENT ──markPaid──▶ PAID
 *        │
 *        └──────cancel───────▶ CANCELLED
 * </pre>
 *
 * 금액은 항상 서버에서 계산한다: totalAmount = itemTotal + shippingCost
 */
public class Order {

    private final OrderId id;
    private final CartId cartId;
    private final List<OrderItem> items;
    private final Money itemTotal;
    private final Money shippingCost;
    private final Money totalAmount;
    private final OrderContact contact;
    private final ShippingAddress shippingAddress;
    private OrderStatus status;
    private CancelReason cancelReason;
    private final Instant createdAt;
    private Instant paidAt;
    private Instant cancelledAt;

    private Order(OrderId id, CartId cartId, List<OrderItem> items, Money itemTotal, Money shippingCost,
                  Money totalAmount, OrderContact contact, ShippingAddress shippingAddress, OrderStatus status,
                  CancelReason cancelReason, Instant createdAt, Instant paidAt, Instant cancelledAt) {
        this.id = Objects.requireNonNull(id);
        this.cartId = Objects.requireNonNull(cartId);
        this.items = List.copyOf(items);
        this.itemTotal = Objects.requireNonNull(itemTotal);
        this.shippingCost = Objects.requireNonNull(shippingCost);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.contact = Objects.requireNonNull(contact);
        this.shippingAddress = Objects.requireNonNull(shippingAddress);
        this.status = Objects.requireNonNull(status);
        this.cancelReason = cancelReason;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.paidAt = paidAt;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 주문 생성 (결제 대기 상태).
     */
    public static Order place(CartId cartId, List<OrderItem> items, Money shippingCost, OrderContact contact,
                              ShippingAddress shippingAddress, Instant now) {
        if (items == null || items.isEmpty()) {
            throw new EmptyOrderException();
        }
        Money itemTotal = items.stream()
            .map(OrderItem::lineTotal)
            .reduce(Money::plus)
            .orElseThrow();
        Money totalAmount = itemTotal.plus(shippingCost);

        return new Order(OrderId.generate(), cartId, items, itemTotal, shippingCost, totalAmount, contact,
            shippingAddress, OrderStatus.PENDING_PAYMENT, null, now, null, null);
    }

    public static Order restore(OrderId id, CartId cartId, List<OrderItem> items, Money itemTotal,
                                Money shippingCost, Money totalAmount, OrderContact contact,
                                ShippingAddress shippingAddress, OrderStatus status, CancelReason cancelReason,
                                Instant createdAt, Instant paidAt, Instant cancelledAt) {
        return new Order(id, cartId, items, itemTotal, shippingCost, totalAmount, contact, shippingAddress,
            status, cancelReason, createdAt, paidAt, cancelledAt);
    }

    /** 결제를 시작할 수 있는 상태인지 검증 */
    public void ensurePayable() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(id, status, "start payment");
        }
    }

    public void markPaid(Instant now) {
        if (status == OrderStatus.PAID) {
            return; // 멱등
        }
        ensurePayable();
        this.status = OrderStatus.PAID;
        this.paidAt = Objects.requireNonNull(now);
    }

    /**
     * 결제 전 취소.
     * @return 실제로 상태가 변경되었으면 true, 이미 취소된 주문이면 false (멱등)
     */
    public boolean cancel(CancelReason reason, Instant now) {
        if (status == OrderStatus.CANCELLED) {
            return false;
        }
        if (status == OrderStatus.PAID) {
            throw new InvalidOrderStatusException(id, status, "cancel before payment");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = Objects.requireNonNull(reason);
        this.cancelledAt = Objects.requireNonNull(now);
        return true;
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public int totalQuantity() {
        return items.stream().mapToInt(OrderItem::quantity).sum();
    }

    public OrderId getId() { return id; }
    public CartId getCartId() { return cartId; }
    public List<OrderItem> getItems() { return items; }
    public Money getItemTotal() { return itemTotal; }
    public Money getShippingCost() { return shippingCost; }
    public Money getTotalAmount() { return totalAmount; }
    public OrderContact getContact() { return contact; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public OrderStatus getStatus() { return status; }
    public CancelReason getCancelReason() { return cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
