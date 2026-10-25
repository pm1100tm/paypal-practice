package com.swd.shopswd.adapter.out.persistence.order;

import com.swd.shopswd.domain.order.value.CancelReason;
import com.swd.shopswd.domain.order.value.OrderStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orders")
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "cart_id", nullable = false, length = 36)
    private String cartId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_item", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "line_no")
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "item_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal itemTotal;

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // --- contact
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    // --- shipping address
    @Column(name = "ship_full_name", nullable = false, length = 300)
    private String shipFullName;

    @Column(name = "ship_address_line1", nullable = false, length = 300)
    private String shipAddressLine1;

    @Column(name = "ship_address_line2", length = 300)
    private String shipAddressLine2;

    @Column(name = "ship_city", nullable = false, length = 120)
    private String shipCity;

    @Column(name = "ship_state", length = 300)
    private String shipState;

    @Column(name = "ship_postal_code", length = 60)
    private String shipPostalCode;

    @Column(name = "ship_country_code", nullable = false, length = 2)
    private String shipCountryCode;

    // --- status
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // MySQL ENUM 대신 VARCHAR — 값 추가 시 DDL 변경 불필요
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR) // MySQL ENUM 대신 VARCHAR — 값 추가 시 DDL 변경 불필요
    @Column(name = "cancel_reason", length = 30)
    private CancelReason cancelReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    private Long version;

    OrderJpaEntity(String id) {
        this.id = id;
    }
}
