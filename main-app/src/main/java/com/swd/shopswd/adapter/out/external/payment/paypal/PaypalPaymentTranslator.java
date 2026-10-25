package com.swd.shopswd.adapter.out.external.payment.paypal;

import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Address;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Amount;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Breakdown;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.ExperienceContext;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Item;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Name;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.PayPal;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.PayPalMoney;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.PaymentSource;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.PurchaseUnit;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest.Shipping;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalOrderResponse;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalOrderResponse.Capture;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.order.value.ShippingAddress;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.value.PaymentErrorType;
import com.swd.shopswd.domain.payment.value.PaymentResult;
import com.swd.shopswd.domain.payment.value.TransactionInfo;
import com.swd.shopswd.domain.shared.value.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 도메인 ↔ PayPal Orders v2 요청/응답 변환 (네트워크 호출 없음).
 */
@Component
public class PaypalPaymentTranslator {

    private final PayPalApiProperties properties;

    public PaypalPaymentTranslator(PayPalApiProperties properties) {
        this.properties = properties;
    }

    /**
     * Create order 요청 생성.
     * amount.value = item_total + shipping, item_total = Σ(unit_amount × quantity) 가 성립해야 한다.
     */
    public PayPalCreateOrderRequest toCreateOrderRequest(Payment payment, Order order) {
        String orderId = order.getId().value();

        PurchaseUnit purchaseUnit = PurchaseUnit.builder()
            .customId(orderId)
            .invoiceId(orderId)
            .amount(Amount.builder()
                .currencyCode(payment.getAmount().currencyCode())
                .value(payment.getAmount().toPlainString())
                .breakdown(Breakdown.builder()
                    .itemTotal(toPayPalMoney(order.getItemTotal()))
                    .shipping(toPayPalMoney(order.getShippingCost()))
                    .build())
                .build())
            .items(toItems(order.getItems()))
            .shipping(toShipping(order.getShippingAddress()))
            .build();

        return PayPalCreateOrderRequest.builder()
            .intent(PayPalConst.INTENT_CAPTURE)
            .purchaseUnits(List.of(purchaseUnit))
            .paymentSource(PaymentSource.builder()
                .paypal(PayPal.builder()
                    .experienceContext(ExperienceContext.builder()
                        .brandName(properties.getBrandName())
                        .shippingPreference(PayPalConst.SHIPPING_PREFERENCE)
                        .userAction(PayPalConst.USER_ACTION_PAY_NOW)
                        .returnUrl(properties.getReturnUrl())
                        .cancelUrl(properties.getCancelUrl())
                        .build())
                    .build())
                .build())
            .build();
    }

    /**
     * Capture(또는 Get order) 응답을 결제 결과로 변환한다.
     *
     * <p>캡처 요청이 이미 PayPal 에 전달된 이후이므로, 결과를 확정할 수 없는 경우는
     * {@link PaymentErrorType#AFTER_CAPTURE} (수동 확인 대상) 로 분류한다.</p>
     */
    public PaymentResult toPaymentResult(String pgOrderId, PayPalOrderResponse response) {
        if (response == null) {
            return PaymentResult.failure("EMPTY_RESPONSE",
                "PayPal capture response is empty: " + pgOrderId, PaymentErrorType.AFTER_CAPTURE);
        }
        if (!pgOrderId.equals(response.id())) {
            return PaymentResult.failure("ID_MISMATCH",
                "PayPal order id mismatch: expected=" + pgOrderId + ", actual=" + response.id(),
                PaymentErrorType.AFTER_CAPTURE);
        }

        Optional<Capture> found = response.firstCapture();
        if (found.isEmpty()) {
            return PaymentResult.failure("CAPTURE_NOT_FOUND",
                "PayPal response has no capture: status=" + response.status(), PaymentErrorType.AFTER_CAPTURE);
        }

        Capture capture = found.get();
        String reason = capture.statusDetails() != null ? capture.statusDetails().reason() : null;
        String status = String.valueOf(capture.status());

        return switch (status) {
            case PayPalConst.STATUS_COMPLETED -> toSuccess(pgOrderId, capture);
            case "DECLINED", "FAILED" -> PaymentResult.declined("CAPTURE_" + status, reason);
            // PENDING 등: 금전 이동이 진행 중이거나 불확실 → 수동 확인
            default -> PaymentResult.failure("CAPTURE_" + status, reason, PaymentErrorType.AFTER_CAPTURE);
        };
    }

    private PaymentResult toSuccess(String pgOrderId, Capture capture) {
        if (capture.amount() == null || !PayPalConst.DEFAULT_CURRENCY.equals(capture.amount().currencyCode())) {
            return PaymentResult.failure("CURRENCY_MISMATCH",
                "Unexpected capture currency: " + (capture.amount() != null ? capture.amount().currencyCode() : null),
                PaymentErrorType.AFTER_CAPTURE);
        }

        Money pgFee = Optional.ofNullable(capture.sellerReceivableBreakdown())
            .map(PayPalOrderResponse.SellerReceivableBreakdown::paypalFee)
            .map(PaypalPaymentTranslator::toMoney)
            .orElse(null);

        return PaymentResult.success(new TransactionInfo(
            pgOrderId,
            toMoney(capture.amount()),
            capture.id(),
            pgFee,
            capture.customId()
        ));
    }

    private static List<Item> toItems(List<OrderItem> items) {
        return items.stream()
            .map(item -> Item.builder()
                .name(truncate(item.productName(), PayPalConst.MAX_ITEM_NAME))
                .unitAmount(toPayPalMoney(item.unitPrice()))
                .quantity(String.valueOf(item.quantity()))
                .sku(String.valueOf(item.productId().value()))
                .category(PayPalConst.ITEM_CATEGORY_PHYSICAL)
                .build())
            .toList();
    }

    private static Shipping toShipping(ShippingAddress address) {
        return Shipping.builder()
            .name(Name.builder()
                .fullName(truncate(address.fullName(), PayPalConst.MAX_FULL_NAME))
                .build())
            .address(Address.builder()
                .addressLine1(truncate(address.addressLine1(), PayPalConst.MAX_ADDRESS_LINE))
                .addressLine2(truncate(address.addressLine2(), PayPalConst.MAX_ADDRESS_LINE))
                .adminArea2(truncate(address.city(), PayPalConst.MAX_ADMIN_AREA_2))
                .adminArea1(truncate(address.state(), PayPalConst.MAX_ADMIN_AREA_1))
                .postalCode(truncate(address.postalCode(), PayPalConst.MAX_POSTAL_CODE))
                .countryCode(address.countryCode())
                .build())
            .build();
    }

    private static PayPalMoney toPayPalMoney(Money money) {
        return PayPalMoney.builder()
            .currencyCode(money.currencyCode())
            .value(money.toPlainString())
            .build();
    }

    private static Money toMoney(PayPalOrderResponse.Amount amount) {
        return Money.of(new BigDecimal(amount.value()), amount.currencyCode());
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
