package com.swd.shopswd.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.exception.InvalidPaymentStatusException;
import com.swd.shopswd.domain.payment.exception.PaymentAmountMismatchException;
import com.swd.shopswd.domain.payment.exception.PaymentMismatchException;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentStatus;
import com.swd.shopswd.domain.payment.value.TransactionInfo;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String PG_ORDER_ID = "5O190127TN364715T";

    private OrderId orderId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        orderId = OrderId.generate();
        payment = Payment.ready(orderId, PaymentMethod.PAYPAL, Money.usd("57.90"), NOW);
        payment.awaitApproval(PG_ORDER_ID, NOW);
    }

    private TransactionInfo tx(String pgOrderId, String amount, String customId) {
        return new TransactionInfo(pgOrderId, Money.usd(amount), "CAPTURE-1", Money.usd("2.00"), customId);
    }

    @Test
    void 금액과_식별자가_일치하면_완료된다() {
        payment.complete(tx(PG_ORDER_ID, "57.90", orderId.value()), NOW);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        // 동일 캡처 재반영은 멱등
        payment.complete(tx(PG_ORDER_ID, "57.90", orderId.value()), NOW);
        assertThat(payment.isCompleted()).isTrue();
    }

    @Test
    void 캡처금액이_다르면_완료되지_않는다() {
        assertThatThrownBy(() -> payment.complete(tx(PG_ORDER_ID, "1.00", orderId.value()), NOW))
            .isInstanceOf(PaymentAmountMismatchException.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING_APPROVAL);
    }

    @Test
    void 다른_PG주문_또는_다른_주문의_캡처는_거부된다() {
        assertThatThrownBy(() -> payment.complete(tx("OTHER", "57.90", orderId.value()), NOW))
            .isInstanceOf(PaymentMismatchException.class);
        assertThatThrownBy(() -> payment.complete(tx(PG_ORDER_ID, "57.90", OrderId.generate().value()), NOW))
            .isInstanceOf(PaymentMismatchException.class);
        assertThatThrownBy(() -> payment.ensurePgOrderId("OTHER"))
            .isInstanceOf(PaymentMismatchException.class);
    }

    @Test
    void 완료된_결제는_취소할_수_없다() {
        payment.complete(tx(PG_ORDER_ID, "57.90", null), NOW);
        assertThatThrownBy(() -> payment.cancel(NOW)).isInstanceOf(InvalidPaymentStatusException.class);
    }

    @Test
    void 취소는_멱등이다() {
        assertThat(payment.cancel(NOW)).isTrue();
        assertThat(payment.cancel(NOW)).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }
}
