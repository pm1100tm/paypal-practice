package com.swd.shopswd.application.service.payment;

import com.swd.shopswd.application.port.in.payment.CancelPaymentUseCase;
import com.swd.shopswd.application.port.in.payment.CompletePaymentCommand;
import com.swd.shopswd.application.port.in.payment.CompletePaymentResult;
import com.swd.shopswd.application.port.in.payment.CompletePaymentUseCase;
import com.swd.shopswd.application.port.in.payment.CreatePaymentCommand;
import com.swd.shopswd.application.port.in.payment.CreatePaymentResult;
import com.swd.shopswd.application.port.in.payment.CreatePaymentUseCase;
import com.swd.shopswd.application.port.out.payment.PaymentGateway;
import com.swd.shopswd.domain.cart.repository.CartRepository;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.exception.OrderNotFoundException;
import com.swd.shopswd.domain.order.repository.OrderRepository;
import com.swd.shopswd.domain.order.value.CancelReason;
import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.order.value.OrderStatus;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.exception.PaymentAlreadyExistsException;
import com.swd.shopswd.domain.payment.exception.PaymentDeclinedException;
import com.swd.shopswd.domain.payment.exception.PaymentExternalException;
import com.swd.shopswd.domain.payment.exception.PaymentFailedException;
import com.swd.shopswd.domain.payment.exception.PaymentNotFoundException;
import com.swd.shopswd.domain.payment.repository.PaymentRepository;
import com.swd.shopswd.domain.payment.value.PaymentErrorType;
import com.swd.shopswd.domain.payment.value.PaymentInitiation;
import com.swd.shopswd.domain.payment.value.PaymentResult;
import com.swd.shopswd.domain.shared.exception.DomainException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 결제 유스케이스 (PayPal).
 *
 * <pre>
 * [FE] createOrder ──▶ POST /orders            → Order(PENDING_PAYMENT)
 * [FE] createOrder ──▶ POST /payments          → PayPal Create Order → Payment(PENDING_APPROVAL)
 * [FE] PayPal 팝업 승인
 * [FE] onApprove   ──▶ POST /payments/capture  → PayPal Capture → Payment(COMPLETED), Order(PAID), Cart 비움
 * [FE] onCancel    ──▶ POST /payments/cancel/paypal → Payment(CANCELLED), Order(CANCELLED)
 * </pre>
 *
 * 트랜잭션 정책: PG 외부 호출은 DB 트랜잭션 밖에서 수행하고,
 * 호출 결과 반영(결제/주문/장바구니 상태 변경)만 하나의 트랜잭션으로 묶는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements CreatePaymentUseCase, CompletePaymentUseCase, CancelPaymentUseCase {

    private static final String INITIATION_TYPE_CLIENT_SDK = "CLIENT_SDK";

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    @Override
    public CreatePaymentResult createPayment(CreatePaymentCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrder(orderId);
        order.ensurePayable();
        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new PaymentAlreadyExistsException(orderId);
        });

        Payment payment = paymentRepository.save(
            Payment.ready(orderId, paymentGateway.method(), order.getTotalAmount(), now()));

        PaymentInitiation initiation;
        try {
            initiation = paymentGateway.initiate(payment, order);
        } catch (PaymentExternalException e) {
            log.error("[PAY][Create] FAIL | orderId={} | code={} | error={}",
                orderId.value(), e.getCode(), e.getMessage());
            transactionTemplate.executeWithoutResult(status -> {
                Payment current = loadPayment(orderId);
                Order currentOrder = loadOrder(orderId);
                current.fail(e.getCode(), e.getMessage(), now());
                paymentRepository.save(current);
                if (currentOrder.cancel(CancelReason.CREATE_PAYMENT_FAILED, now())) {
                    orderRepository.save(currentOrder);
                }
            });
            throw e;
        }

        payment.awaitApproval(initiation.pgOrderId(), now());
        paymentRepository.save(payment);

        log.info("[PAY][Create] OK | orderId={} | pgOrderId={} | amount={}",
            orderId.value(), initiation.pgOrderId(), payment.getAmount());
        return new CreatePaymentResult(
            orderId.value(),
            initiation.pgOrderId(),
            payment.getMethod().name(),
            INITIATION_TYPE_CLIENT_SDK,
            initiation.approveUrl()
        );
    }

    @Override
    public CompletePaymentResult completePayment(CompletePaymentCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Payment payment = loadPayment(orderId);
        payment.ensurePgOrderId(command.paymentId());

        Order order = loadOrder(orderId);
        if (payment.isCompleted() && order.isPaid()) {
            log.info("[PAY][Capture] already completed | orderId={}", orderId.value());
            return new CompletePaymentResult(orderId.value(), order.getStatus());
        }
        order.ensurePayable();

        // NOTE: PG 호출은 트랜잭션 밖. 통신 장애(PaymentExternalException)는 캡처 여부가 불확실하므로
        //       상태를 변경하지 않고 그대로 전파한다 (PayPal-Request-Id 멱등키로 재시도 안전).
        PaymentResult result = paymentGateway.capture(payment);

        DomainException failure = transactionTemplate.execute(status -> applyCaptureResult(orderId, result));
        if (failure != null) {
            throw failure;
        }

        log.info("[PAY][Capture] OK | orderId={}", orderId.value());
        return new CompletePaymentResult(orderId.value(), OrderStatus.PAID);
    }

    /**
     * 캡처 결과를 결제/주문/장바구니에 반영한다.
     *
     * <p>결제/주문은 트랜잭션 안에서 다시 조회한다 — 같은 영속성 컨텍스트에서 수정되어야
     * {@code @Version} 낙관적 락이 동시 캡처/취소 요청을 감지한다.
     * (예: 취소가 먼저 커밋되었다면 complete 가 실패하여 REVIEW_REQUIRED 로 남는다)</p>
     *
     * <p>실패 상태도 커밋되어야 하므로 예외를 던지지 않고 반환한다 (호출부에서 트랜잭션 종료 후 throw).</p>
     *
     * @return 실패 시 클라이언트에 전달할 예외, 성공 시 null
     */
    private DomainException applyCaptureResult(OrderId id, PaymentResult result) {
        Instant now = now();
        String orderId = id.value();
        Payment payment = loadPayment(id);
        Order order = loadOrder(id);
        if (payment.isCompleted() && order.isPaid()) {
            return null; // 동시 요청이 먼저 완료 처리함 (멱등)
        }

        switch (result) {
            case PaymentResult.Success success -> {
                try {
                    order.ensurePayable(); // 캡처 사이에 주문이 취소되었을 수 있음
                    payment.complete(success.transactionInfo(), now);
                } catch (DomainException e) {
                    // 캡처는 성공했으나 금액/식별자 검증 실패 → 금전 이동 발생, 수동 확인 필요
                    log.error("[PAY][Capture] REVIEW_REQUIRED | orderId={} | code={} | error={}",
                        orderId, e.getCode(), e.getMessage());
                    payment.requireReview(e.getCode(), e.getMessage(), success.transactionInfo(), now);
                    paymentRepository.save(payment);
                    return e;
                }
                order.markPaid(now);
                paymentRepository.save(payment);
                orderRepository.save(order);
                cartRepository.findById(order.getCartId()).ifPresent(cart -> {
                    cart.clear(now);
                    cartRepository.save(cart);
                });
                return null;
            }
            case PaymentResult.Declined declined -> {
                log.info("[PAY][Capture] DECLINED | orderId={} | code={}", orderId, declined.code());
                payment.decline(declined.code(), declined.message(), now);
                order.cancel(CancelReason.PG_DECLINED, now);
                paymentRepository.save(payment);
                orderRepository.save(order);
                return new PaymentDeclinedException(declined.code());
            }
            case PaymentResult.Failed failed when failed.errorType() == PaymentErrorType.AFTER_CAPTURE -> {
                log.error("[PAY][Capture] REVIEW_REQUIRED | orderId={} | code={} | message={}",
                    orderId, failed.code(), failed.message());
                payment.requireReview(failed.code(), failed.message(), null, now);
                paymentRepository.save(payment);
                return new PaymentFailedException("PAYMENT_REVIEW_REQUIRED",
                    "Payment could not be confirmed. Please contact customer service with your order number.");
            }
            case PaymentResult.Failed failed -> {
                log.warn("[PAY][Capture] FAILED | orderId={} | code={} | message={}",
                    orderId, failed.code(), failed.message());
                payment.fail(failed.code(), failed.message(), now);
                order.cancel(CancelReason.PG_ERROR, now);
                paymentRepository.save(payment);
                orderRepository.save(order);
                return new PaymentFailedException("PAYMENT_FAILED", "The payment could not be completed.");
            }
        }
    }

    @Override
    public void cancelBeforePayment(String orderIdValue, CancelReason reason) {
        OrderId orderId = OrderId.of(orderIdValue);

        transactionTemplate.executeWithoutResult(status -> {
            Instant now = now();
            Order order = loadOrder(orderId);
            paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
                if (payment.cancel(now)) {
                    paymentRepository.save(payment);
                }
            });
            if (order.cancel(reason, now)) {
                orderRepository.save(order);
            }
        });
        log.info("[PAY][Cancel] orderId={} | reason={}", orderId.value(), reason);
    }

    private Payment loadPayment(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException(orderId));
    }

    private Order loadOrder(OrderId orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private Instant now() {
        return clock.instant();
    }
}
