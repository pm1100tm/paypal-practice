package com.swd.shopswd.adapter.in.web.api;

import com.swd.shopswd.adapter.in.web.dto.payment.CancelPaypalRequest;
import com.swd.shopswd.adapter.in.web.dto.payment.CapturePaymentRequest;
import com.swd.shopswd.adapter.in.web.dto.payment.CapturePaymentResponse;
import com.swd.shopswd.adapter.in.web.dto.payment.CreatePaymentRequest;
import com.swd.shopswd.adapter.in.web.dto.payment.CreatePaymentResponse;
import com.swd.shopswd.adapter.in.web.dto.payment.ErrorPaypalRequest;
import com.swd.shopswd.adapter.in.web.dto.payment.PaymentCancelResponse;
import com.swd.shopswd.application.port.in.payment.CancelPaymentUseCase;
import com.swd.shopswd.application.port.in.payment.CompletePaymentCommand;
import com.swd.shopswd.application.port.in.payment.CompletePaymentUseCase;
import com.swd.shopswd.application.port.in.payment.CreatePaymentCommand;
import com.swd.shopswd.application.port.in.payment.CreatePaymentUseCase;
import com.swd.shopswd.domain.order.value.CancelReason;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment Controller (PayPal)
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final CompletePaymentUseCase completePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;

    /** PayPal 주문 생성 → paymentId(PayPal Order ID) 를 SDK 에 반환 */
    @PostMapping
    public CreatePaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return CreatePaymentResponse.from(
            createPaymentUseCase.createPayment(new CreatePaymentCommand(request.orderId())));
    }

    /** 구매자 승인(onApprove) 이후 캡처 */
    @PostMapping("/capture")
    public CapturePaymentResponse capturePayment(@Valid @RequestBody CapturePaymentRequest request) {
        return CapturePaymentResponse.from(completePaymentUseCase.completePayment(
            new CompletePaymentCommand(request.orderId(), request.paymentId())));
    }

    /**
     * PayPal 결제 전 취소 — 사용자가 PayPal 팝업에서 취소
     * 장바구니는 결제 완료 전까지 비우지 않으므로 별도 복원 없음
     */
    @PostMapping("/cancel/paypal")
    public PaymentCancelResponse cancelPaypal(@Valid @RequestBody CancelPaypalRequest request) {
        log.info("[PAY][Cancel] START | orderId={} | gateway=PayPal", request.orderId());
        try {
            cancelPaymentUseCase.cancelBeforePayment(request.orderId(), CancelReason.USER_CANCELLED);
            return PaymentCancelResponse.ok();
        } catch (RuntimeException e) {
            log.warn("[PAY][Cancel] FAIL | orderId={} | error={}", request.orderId(), e.getMessage());
            return PaymentCancelResponse.fail(e.getMessage());
        }
    }

    /**
     * PayPal 결제 에러 — PayPal SDK 결제 중 에러 발생
     */
    @PostMapping("/error/paypal")
    public PaymentCancelResponse errorPaypal(@Valid @RequestBody ErrorPaypalRequest request) {
        log.warn("[PAY][Error] START | orderId={} | gateway=PayPal | error={} | message={}",
            request.orderId(), request.errorName(), request.errorMessage());
        try {
            cancelPaymentUseCase.cancelBeforePayment(request.orderId(), CancelReason.CLIENT_ERROR);
            return PaymentCancelResponse.ok();
        } catch (RuntimeException e) {
            log.warn("[PAY][Error] cancel FAIL | orderId={} | error={}", request.orderId(), e.getMessage());
            return PaymentCancelResponse.fail(e.getMessage());
        }
    }
}
