package com.swd.shopswd.domain.payment.aggregate;

import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.payment.exception.InvalidPaymentStatusException;
import com.swd.shopswd.domain.payment.exception.PaymentAmountMismatchException;
import com.swd.shopswd.domain.payment.exception.PaymentMismatchException;
import com.swd.shopswd.domain.payment.value.PaymentId;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentStatus;
import com.swd.shopswd.domain.payment.value.TransactionInfo;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Instant;
import java.util.Objects;

/**
 * Payment Aggregate Root.
 *
 * 상태 전이:
 * <pre>
 * READY ──awaitApproval──▶ PENDING_APPROVAL ──complete──▶ COMPLETED
 *   │                            │
 *   ├──fail / cancel             ├──decline / fail / cancel
 *   │                            └──requireReview (캡처 후 검증 실패)
 * </pre>
 */
public class Payment {

    private final PaymentId id;
    private final OrderId orderId;
    private final PaymentMethod method;
    private final Money amount;
    private PaymentStatus status;
    private String pgOrderId;
    private TransactionInfo transactionInfo;
    private String failureCode;
    private String failureMessage;
    private final Instant createdAt;
    private Instant updatedAt;

    private Payment(PaymentId id, OrderId orderId, PaymentMethod method, Money amount, PaymentStatus status,
                    String pgOrderId, TransactionInfo transactionInfo, String failureCode, String failureMessage,
                    Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.orderId = Objects.requireNonNull(orderId);
        this.method = Objects.requireNonNull(method);
        this.amount = Objects.requireNonNull(amount);
        this.status = Objects.requireNonNull(status);
        this.pgOrderId = pgOrderId;
        this.transactionInfo = transactionInfo;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Payment ready(OrderId orderId, PaymentMethod method, Money amount, Instant now) {
        if (amount.isZero()) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        return new Payment(PaymentId.generate(), orderId, method, amount, PaymentStatus.READY,
            null, null, null, null, now, now);
    }

    public static Payment restore(PaymentId id, OrderId orderId, PaymentMethod method, Money amount,
                                  PaymentStatus status, String pgOrderId, TransactionInfo transactionInfo,
                                  String failureCode, String failureMessage, Instant createdAt, Instant updatedAt) {
        return new Payment(id, orderId, method, amount, status, pgOrderId, transactionInfo, failureCode,
            failureMessage, createdAt, updatedAt);
    }

    /** PG 주문 생성 완료 → 구매자 승인 대기 */
    public void awaitApproval(String pgOrderId, Instant now) {
        requireStatus(PaymentStatus.READY, "await approval");
        this.pgOrderId = Objects.requireNonNull(pgOrderId);
        this.status = PaymentStatus.PENDING_APPROVAL;
        touch(now);
    }

    /**
     * 클라이언트가 전달한 PG 주문 ID 가 이 결제의 것인지 검증.
     * (다른 주문의 PayPal 승인으로 이 주문을 결제 완료 처리하는 것을 차단)
     */
    public void ensurePgOrderId(String candidate) {
        if (pgOrderId == null || !pgOrderId.equals(candidate)) {
            throw new PaymentMismatchException(orderId);
        }
    }

    /**
     * 캡처 성공 반영.
     * PG 주문 ID 및 캡처 금액이 주문 금액과 정확히 일치해야 한다.
     * 불일치 시 상태를 변경하지 않고 예외를 던진다 → 호출자가 {@link #requireReview} 로 처리.
     */
    public void complete(TransactionInfo info, Instant now) {
        Objects.requireNonNull(info);
        if (status == PaymentStatus.COMPLETED
            && transactionInfo != null
            && transactionInfo.captureId().equals(info.captureId())) {
            return; // 멱등
        }
        requireStatus(PaymentStatus.PENDING_APPROVAL, "complete");
        ensurePgOrderId(info.pgOrderId());
        if (info.customId() != null && !info.customId().equals(orderId.value())) {
            throw new PaymentMismatchException(orderId);
        }
        if (!info.capturedAmount().currency().equals(amount.currency())
            || info.capturedAmount().compareTo(amount) != 0) {
            throw new PaymentAmountMismatchException(orderId, amount, info.capturedAmount());
        }
        this.transactionInfo = info;
        this.status = PaymentStatus.COMPLETED;
        touch(now);
    }

    public void decline(String code, String message, Instant now) {
        requireStatus(PaymentStatus.PENDING_APPROVAL, "decline");
        markFailure(PaymentStatus.DECLINED, code, message, now);
    }

    public void fail(String code, String message, Instant now) {
        if (status.isTerminal()) {
            throw new InvalidPaymentStatusException(id, status, "fail");
        }
        markFailure(PaymentStatus.FAILED, code, message, now);
    }

    /** 캡처 이후 검증 실패 — 금전 이동이 발생했을 수 있어 수동 확인이 필요 */
    public void requireReview(String code, String message, TransactionInfo info, Instant now) {
        if (status == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException(id, status, "require review");
        }
        this.transactionInfo = info;
        markFailure(PaymentStatus.REVIEW_REQUIRED, code, message, now);
    }

    /**
     * 사용자 취소.
     * @return 실제로 상태가 변경되었으면 true (이미 종료된 결제면 false)
     */
    public boolean cancel(Instant now) {
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.REVIEW_REQUIRED) {
            throw new InvalidPaymentStatusException(id, status, "cancel");
        }
        if (status.isTerminal()) {
            return false;
        }
        this.status = PaymentStatus.CANCELLED;
        touch(now);
        return true;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    private void markFailure(PaymentStatus newStatus, String code, String message, Instant now) {
        this.status = newStatus;
        this.failureCode = code;
        this.failureMessage = message;
        touch(now);
    }

    private void requireStatus(PaymentStatus expected, String action) {
        if (status != expected) {
            throw new InvalidPaymentStatusException(id, status, action);
        }
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now);
    }

    public PaymentId getId() { return id; }
    public OrderId getOrderId() { return orderId; }
    public PaymentMethod getMethod() { return method; }
    public Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPgOrderId() { return pgOrderId; }
    public TransactionInfo getTransactionInfo() { return transactionInfo; }
    public String getFailureCode() { return failureCode; }
    public String getFailureMessage() { return failureMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
