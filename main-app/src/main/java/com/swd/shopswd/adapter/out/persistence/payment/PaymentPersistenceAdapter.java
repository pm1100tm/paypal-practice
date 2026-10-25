package com.swd.shopswd.adapter.out.persistence.payment;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.repository.PaymentRepository;
import com.swd.shopswd.domain.payment.value.PaymentId;
import com.swd.shopswd.domain.payment.value.TransactionInfo;
import com.swd.shopswd.domain.shared.value.Money;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository {

    private static final int FAILURE_MESSAGE_MAX = 1000;

    private final PaymentJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.value()).map(PaymentPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = jpaRepository.findById(payment.getId().value())
            .orElseGet(() -> new PaymentJpaEntity(payment.getId().value()));

        entity.setOrderId(payment.getOrderId().value());
        entity.setMethod(payment.getMethod());
        entity.setCurrency(payment.getAmount().currencyCode());
        entity.setAmount(payment.getAmount().amount());
        entity.setStatus(payment.getStatus());
        entity.setPgOrderId(payment.getPgOrderId());

        TransactionInfo tx = payment.getTransactionInfo();
        entity.setCaptureId(tx != null ? tx.captureId() : null);
        entity.setCapturedAmount(tx != null ? tx.capturedAmount().amount() : null);
        entity.setCapturedCurrency(tx != null ? tx.capturedAmount().currencyCode() : null);
        entity.setPgFee(tx != null && tx.pgFee() != null ? tx.pgFee().amount() : null);
        entity.setPgCustomId(tx != null ? tx.customId() : null);

        entity.setFailureCode(payment.getFailureCode());
        entity.setFailureMessage(truncate(payment.getFailureMessage()));
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());

        return toDomain(jpaRepository.saveAndFlush(entity));
    }

    private static Payment toDomain(PaymentJpaEntity entity) {
        TransactionInfo tx = null;
        if (entity.getCaptureId() != null && entity.getCapturedAmount() != null) {
            tx = new TransactionInfo(
                entity.getPgOrderId(),
                Money.of(entity.getCapturedAmount(), entity.getCapturedCurrency()),
                entity.getCaptureId(),
                entity.getPgFee() != null ? Money.of(entity.getPgFee(), entity.getCapturedCurrency()) : null,
                entity.getPgCustomId()
            );
        }
        return Payment.restore(
            PaymentId.of(entity.getId()),
            OrderId.of(entity.getOrderId()),
            entity.getMethod(),
            Money.of(entity.getAmount(), entity.getCurrency()),
            entity.getStatus(),
            entity.getPgOrderId(),
            tx,
            entity.getFailureCode(),
            entity.getFailureMessage(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= FAILURE_MESSAGE_MAX) {
            return value;
        }
        return value.substring(0, FAILURE_MESSAGE_MAX);
    }
}
