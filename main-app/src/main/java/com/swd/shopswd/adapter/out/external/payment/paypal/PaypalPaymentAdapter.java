package com.swd.shopswd.adapter.out.external.payment.paypal;

import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalErrorResponse;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalOrderResponse;
import com.swd.shopswd.application.port.out.payment.PaymentGateway;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.exception.PaymentExternalException;
import com.swd.shopswd.domain.payment.value.PaymentErrorType;
import com.swd.shopswd.domain.payment.value.PaymentInitiation;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

/**
 * PayPal {@link PaymentGateway} 구현.
 *
 * <p>에러 처리 정책:</p>
 * <ul>
 *     <li>Create 실패 → {@link PaymentExternalException} (금전 이동 없음)</li>
 *     <li>Capture 4xx → PayPal 이 요청을 처리하지 않았으므로 {@link PaymentResult} 로 반환
 *         (INSTRUMENT_DECLINED 는 Declined, ORDER_ALREADY_CAPTURED 는 주문 조회로 복구)</li>
 *     <li>Capture 5xx / 네트워크 장애 → 캡처 여부를 알 수 없으므로 {@link PaymentExternalException}
 *         (PayPal-Request-Id 멱등키로 재시도 안전)</li>
 * </ul>
 */
@Slf4j
@Component
public class PaypalPaymentAdapter implements PaymentGateway {

    private final PayPalClient payPalClient;
    private final PayPalAccessTokenProvider tokenProvider;
    private final PaypalPaymentTranslator translator;

    public PaypalPaymentAdapter(PayPalClient payPalClient, PayPalAccessTokenProvider tokenProvider,
                                PaypalPaymentTranslator translator) {
        this.payPalClient = payPalClient;
        this.tokenProvider = tokenProvider;
        this.translator = translator;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public PaymentInitiation initiate(Payment payment, Order order) {
        String orderId = payment.getOrderId().value();
        String accessToken = tokenProvider.getToken();

        PayPalOrderResponse response;
        try {
            response = payPalClient.createOrder(accessToken, translator.toCreateOrderRequest(payment, order),
                PayPalConst.requestIdForCreate(payment.getId().value()));
        } catch (HttpStatusCodeException e) {
            PayPalErrorResponse error = parseError(e);
            invalidateTokenIfUnauthorized(e);
            log.error("[PAY][PG:PayPal] createOrder API error: orderId={}, status={}, name={}, issue={}, debugId={}",
                orderId, e.getStatusCode().value(), nameOf(error), issueOf(error), debugIdOf(error));
            String code = e instanceof HttpClientErrorException ? "PAYPAL_CREATE_REJECTED" : "PAYPAL_CREATE_SERVER_ERROR";
            throw new PaymentExternalException(code, "PayPal order creation failed", e);
        } catch (RestClientException e) {
            log.error("[PAY][PG:PayPal] createOrder network error: orderId={}, error={}", orderId, e.getMessage());
            throw new PaymentExternalException("PAYPAL_CREATE_NETWORK_ERROR", "PayPal network error on create order", e);
        }

        if (response == null || response.id() == null) {
            throw new PaymentExternalException("PAYPAL_CREATE_EMPTY", "PayPal order creation response is empty");
        }
        return new PaymentInitiation(response.id(), response.approveUrl().orElse(null));
    }

    @Override
    public PaymentResult capture(Payment payment) {
        String orderId = payment.getOrderId().value();
        String pgOrderId = payment.getPgOrderId();
        String accessToken = tokenProvider.getToken();

        try {
            return translator.toPaymentResult(pgOrderId, payPalClient.captureOrder(accessToken, pgOrderId));
        } catch (HttpClientErrorException e) {
            return handleCaptureClientError(e, accessToken, orderId, pgOrderId);
        } catch (HttpStatusCodeException e) {
            PayPalErrorResponse error = parseError(e);
            log.error("[PAY][PG:PayPal] captureOrder API error: orderId={}, status={}, name={}, debugId={}",
                orderId, e.getStatusCode().value(), nameOf(error), debugIdOf(error));
            throw new PaymentExternalException("PAYPAL_CAPTURE_SERVER_ERROR", "PayPal server error on capture order", e);
        } catch (RestClientException e) {
            log.error("[PAY][PG:PayPal] captureOrder network error: orderId={}, error={}", orderId, e.getMessage());
            throw new PaymentExternalException("PAYPAL_CAPTURE_NETWORK_ERROR", "PayPal network error on capture order", e);
        }
    }

    private PaymentResult handleCaptureClientError(HttpClientErrorException e, String accessToken,
                                                   String orderId, String pgOrderId) {
        PayPalErrorResponse error = parseError(e);
        String issue = issueOf(error);

        if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            // 토큰 만료/폐기 — 캡처는 처리되지 않았으므로 재시도 가능하도록 예외로 전파
            tokenProvider.invalidate();
            throw new PaymentExternalException("PAYPAL_UNAUTHORIZED", "PayPal access token rejected", e);
        }
        if (PayPalConst.ISSUE_ORDER_ALREADY_CAPTURED.equals(issue)) {
            log.info("[PAY][PG:PayPal] ORDER_ALREADY_CAPTURED: orderId={}, pgOrderId={}", orderId, pgOrderId);
            return recoverAlreadyCaptured(accessToken, orderId, pgOrderId);
        }
        if (PayPalConst.ISSUE_INSTRUMENT_DECLINED.equals(issue)) {
            log.info("[PAY][PG:PayPal] capture declined: orderId={}, issue={}", orderId, issue);
            return PaymentResult.declined(issue, messageOf(error, e));
        }

        log.error("[PAY][PG:PayPal] captureOrder API error: orderId={}, status={}, name={}, issue={}, debugId={}",
            orderId, e.getStatusCode().value(), nameOf(error), issue, debugIdOf(error));
        String code = issue != null ? issue : nameOf(error);
        return PaymentResult.failure(code, messageOf(error, e), PaymentErrorType.BEFORE_CAPTURE);
    }

    /**
     * 이미 캡처된 주문 — 주문 상세의 캡처 정보로 결과를 복구한다 (재시도/동시 요청 대비).
     */
    private PaymentResult recoverAlreadyCaptured(String accessToken, String orderId, String pgOrderId) {
        try {
            return translator.toPaymentResult(pgOrderId, payPalClient.getOrderDetail(accessToken, pgOrderId));
        } catch (RestClientException e) {
            log.error("[PAY][PG:PayPal] getOrderDetail failed: orderId={}, error={}", orderId, e.getMessage());
            throw new PaymentExternalException("PAYPAL_ORDER_DETAIL_FAILED",
                "Failed to load already captured PayPal order", e);
        }
    }

    private void invalidateTokenIfUnauthorized(HttpStatusCodeException e) {
        if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            tokenProvider.invalidate();
        }
    }

    private static PayPalErrorResponse parseError(HttpStatusCodeException e) {
        try {
            return e.getResponseBodyAs(PayPalErrorResponse.class);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String nameOf(PayPalErrorResponse error) {
        return error != null && error.name() != null ? error.name() : "UNKNOWN";
    }

    private static String issueOf(PayPalErrorResponse error) {
        return error != null ? error.firstIssue() : null;
    }

    private static String debugIdOf(PayPalErrorResponse error) {
        return error != null ? error.debugId() : null;
    }

    private static String messageOf(PayPalErrorResponse error, HttpStatusCodeException e) {
        return error != null && error.message() != null ? error.message() : e.getStatusText();
    }
}
