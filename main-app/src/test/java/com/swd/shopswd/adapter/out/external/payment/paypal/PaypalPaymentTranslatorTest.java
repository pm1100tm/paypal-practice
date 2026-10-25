package com.swd.shopswd.adapter.out.external.payment.paypal;

import static org.assertj.core.api.Assertions.assertThat;

import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalOrderResponse;
import com.swd.shopswd.domain.order.OrderFixtures;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.value.PaymentErrorType;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentResult;
import com.swd.shopswd.domain.shared.value.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * PayPal 요청/응답 변환 테스트 (네트워크 호출 없음).
 */
class PaypalPaymentTranslatorTest {

    private static final String PG_ORDER_ID = "5O190127TN364715T";

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private PaypalPaymentTranslator translator;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        PayPalApiProperties properties = new PayPalApiProperties();
        properties.setBrandName("ShopSWD");
        properties.setReturnUrl("http://localhost:3000/order/checkout");
        properties.setCancelUrl("http://localhost:3000/cart");
        translator = new PaypalPaymentTranslator(properties);

        order = OrderFixtures.pendingOrder();
        payment = Payment.ready(order.getId(), PaymentMethod.PAYPAL, order.getTotalAmount(), OrderFixtures.NOW);
    }

    @Test
    void Create_요청의_금액은_breakdown_합계와_일치한다() {
        PayPalCreateOrderRequest request = translator.toCreateOrderRequest(payment, order);
        JsonNode json = objectMapper.valueToTree(request);
        JsonNode unit = json.get("purchase_units").get(0);

        assertThat(json.get("intent").asString()).isEqualTo("CAPTURE");
        assertThat(unit.get("custom_id").asString()).isEqualTo(order.getId().value());
        assertThat(unit.get("invoice_id").asString()).isEqualTo(order.getId().value());

        JsonNode amount = unit.get("amount");
        assertThat(amount.get("currency_code").asString()).isEqualTo("USD");
        assertThat(amount.get("value").asString()).isEqualTo("57.90");
        assertThat(amount.get("breakdown").get("item_total").get("value").asString()).isEqualTo("52.90");
        assertThat(amount.get("breakdown").get("shipping").get("value").asString()).isEqualTo("5.00");

        // item_total == Σ(unit_amount × quantity)
        BigDecimal sum = BigDecimal.ZERO;
        for (JsonNode item : unit.get("items")) {
            sum = sum.add(new BigDecimal(item.get("unit_amount").get("value").asString())
                .multiply(new BigDecimal(item.get("quantity").asString())));
        }
        assertThat(sum).isEqualByComparingTo("52.90");

        JsonNode address = unit.get("shipping").get("address");
        assertThat(address.get("country_code").asString()).isEqualTo("US");
        assertThat(address.has("address_line_2")).as("null 필드는 직렬화하지 않는다").isFalse();

        JsonNode experience = json.get("payment_source").get("paypal").get("experience_context");
        assertThat(experience.get("shipping_preference").asString()).isEqualTo("SET_PROVIDED_ADDRESS");
    }

    @Test
    void Capture_COMPLETED_응답은_Success() {
        PaymentResult result = translator.toPaymentResult(PG_ORDER_ID, captureResponse("COMPLETED", "57.90", "USD"));

        assertThat(result).isInstanceOf(PaymentResult.Success.class);
        var tx = ((PaymentResult.Success) result).transactionInfo();
        assertThat(tx.capturedAmount()).isEqualTo(Money.usd("57.90"));
        assertThat(tx.captureId()).isEqualTo("3C679366HH908993F");
        assertThat(tx.pgFee()).isEqualTo(Money.usd("2.33"));
        assertThat(tx.customId()).isEqualTo("order-123");
    }

    @Test
    void Capture_DECLINED_응답은_Declined() {
        PaymentResult result = translator.toPaymentResult(PG_ORDER_ID, captureResponse("DECLINED", "57.90", "USD"));
        assertThat(result).isInstanceOf(PaymentResult.Declined.class);
    }

    @Test
    void Capture_PENDING_또는_통화불일치는_수동확인_대상() {
        PaymentResult pending = translator.toPaymentResult(PG_ORDER_ID, captureResponse("PENDING", "57.90", "USD"));
        PaymentResult currency = translator.toPaymentResult(PG_ORDER_ID, captureResponse("COMPLETED", "57.90", "EUR"));

        assertThat(pending).isInstanceOfSatisfying(PaymentResult.Failed.class,
            f -> assertThat(f.errorType()).isEqualTo(PaymentErrorType.AFTER_CAPTURE));
        assertThat(currency).isInstanceOfSatisfying(PaymentResult.Failed.class,
            f -> assertThat(f.code()).isEqualTo("CURRENCY_MISMATCH"));
    }

    @Test
    void 응답_ID가_다르면_실패() {
        PaymentResult result = translator.toPaymentResult("OTHER", captureResponse("COMPLETED", "57.90", "USD"));
        assertThat(result).isInstanceOfSatisfying(PaymentResult.Failed.class,
            f -> assertThat(f.code()).isEqualTo("ID_MISMATCH"));
    }

    private PayPalOrderResponse captureResponse(String captureStatus, String value, String currency) {
        String json = """
            {
              "id": "%s",
              "status": "COMPLETED",
              "purchase_units": [{
                "reference_id": "default",
                "payments": {
                  "captures": [{
                    "id": "3C679366HH908993F",
                    "status": "%s",
                    "amount": { "currency_code": "%s", "value": "%s" },
                    "custom_id": "order-123",
                    "seller_receivable_breakdown": {
                      "gross_amount": { "currency_code": "USD", "value": "57.90" },
                      "paypal_fee": { "currency_code": "USD", "value": "2.33" },
                      "net_amount": { "currency_code": "USD", "value": "55.57" }
                    },
                    "unknown_field": true
                  }]
                }
              }],
              "payer": { "email_address": "buyer@example.com" },
              "links": []
            }
            """.formatted(PG_ORDER_ID, captureStatus, currency, value);
        return objectMapper.readValue(json, PayPalOrderResponse.class);
    }
}
