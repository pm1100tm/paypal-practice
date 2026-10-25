package com.swd.shopswd.adapter.out.persistence.payment;

import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "payment",
    uniqueConstraints = @UniqueConstraint(name = "uk_payment_order_id", columnNames = "order_id")
)
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // MySQL ENUM 대신 VARCHAR — 값 추가 시 DDL 변경 불필요
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // MySQL ENUM 대신 VARCHAR — 값 추가 시 DDL 변경 불필요
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "pg_order_id", length = 64)
    private String pgOrderId;

    // --- transaction info (capture)
    @Column(name = "capture_id", length = 64)
    private String captureId;

    @Column(name = "captured_amount", precision = 12, scale = 2)
    private BigDecimal capturedAmount;

    @Column(name = "captured_currency", length = 3)
    private String capturedCurrency;

    @Column(name = "pg_fee", precision = 12, scale = 2)
    private BigDecimal pgFee;

    @Column(name = "pg_custom_id", length = 127)
    private String pgCustomId;

    // --- failure
    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    PaymentJpaEntity(String id) {
        this.id = id;
    }
}
