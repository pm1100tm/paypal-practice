package com.swd.shopswd.domain.order;

import static com.swd.shopswd.domain.order.OrderFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.exception.EmptyOrderException;
import com.swd.shopswd.domain.order.exception.InvalidOrderStatusException;
import com.swd.shopswd.domain.order.value.CancelReason;
import com.swd.shopswd.domain.order.value.OrderContact;
import com.swd.shopswd.domain.order.value.OrderStatus;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void 주문_금액은_상품합계와_배송비의_합이다() {
        Order order = OrderFixtures.pendingOrder();

        assertThat(order.getItemTotal()).isEqualTo(Money.usd("52.90"));
        assertThat(order.getTotalAmount()).isEqualTo(Money.usd("57.90"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getShippingAddress().countryCode()).isEqualTo("US");
    }

    @Test
    void 빈_주문은_생성할_수_없다() {
        assertThatThrownBy(() -> Order.place(CartId.generate(), List.of(), Money.usd("0"),
            new OrderContact("a@b.co", null), OrderFixtures.address(), NOW))
            .isInstanceOf(EmptyOrderException.class);
    }

    @Test
    void 결제완료_주문은_취소할_수_없다() {
        Order order = OrderFixtures.pendingOrder();
        order.markPaid(NOW);

        assertThatThrownBy(() -> order.cancel(CancelReason.USER_CANCELLED, NOW))
            .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void 취소된_주문은_결제할_수_없고_재취소는_멱등이다() {
        Order order = OrderFixtures.pendingOrder();
        assertThat(order.cancel(CancelReason.USER_CANCELLED, NOW)).isTrue();
        assertThat(order.cancel(CancelReason.USER_CANCELLED, NOW)).isFalse();

        assertThatThrownBy(() -> order.markPaid(NOW)).isInstanceOf(InvalidOrderStatusException.class);
    }
}
