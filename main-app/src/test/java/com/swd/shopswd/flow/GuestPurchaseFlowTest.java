package com.swd.shopswd.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.swd.shopswd.application.port.out.payment.PaymentGateway;
import com.swd.shopswd.domain.order.value.OrderId;
import com.swd.shopswd.domain.payment.aggregate.Payment;
import com.swd.shopswd.domain.payment.repository.PaymentRepository;
import com.swd.shopswd.domain.payment.value.PaymentInitiation;
import com.swd.shopswd.domain.payment.value.PaymentMethod;
import com.swd.shopswd.domain.payment.value.PaymentResult;
import com.swd.shopswd.domain.payment.value.PaymentStatus;
import com.swd.shopswd.domain.payment.value.TransactionInfo;
import com.swd.shopswd.domain.shared.value.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 비회원 구매 흐름 E2E (API 레벨).
 * PaymentGateway(PayPal) 는 Mock 으로 대체 — 실제 PayPal 을 호출하지 않는다.
 * DB 는 H2 인메모리 (profile: test) — 로컬 MySQL 없이 실행된다.
 */
@SpringBootTest
@ActiveProfiles("test")
class GuestPurchaseFlowTest {

    private static final String PG_ORDER_ID = "MOCK-PAYPAL-ORDER-1";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentGateway paymentGateway;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        given(paymentGateway.method()).willReturn(PaymentMethod.PAYPAL);
        given(paymentGateway.initiate(any(), any())).willReturn(new PaymentInitiation(PG_ORDER_ID, null));
    }

    @Test
    void 장바구니_체크아웃_PayPal_캡처까지_완료된다() throws Exception {
        String cartId = createCartWithItems();

        // 체크아웃 미리보기: 18.50 x 2 + 15.90 = 52.90
        mockMvc.perform(post("/api/v2/orders/checkout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cartId\":\"" + cartId + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemTotal.amount").value("52.90"))
            .andExpect(jsonPath("$.totalAmount.amount").value("52.90"));

        String orderId = placeOrder(cartId);

        // PayPal 주문 생성
        mockMvc.perform(post("/api/v2/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\",\"paymentMethod\":\"PAYPAL\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(PG_ORDER_ID))
            .andExpect(jsonPath("$.initiationType").value("CLIENT_SDK"));

        // 캡처 성공
        given(paymentGateway.capture(any())).willAnswer(inv -> {
            Payment payment = inv.getArgument(0);
            return PaymentResult.success(new TransactionInfo(PG_ORDER_ID, payment.getAmount(), "CAPTURE-1",
                Money.usd("1.00"), orderId));
        });
        mockMvc.perform(post("/api/v2/payments/capture").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\",\"paymentId\":\"" + PG_ORDER_ID + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));

        // 주문 완료 조회 + 장바구니 비워짐
        mockMvc.perform(get("/api/v2/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.totalAmount.amount").value("52.90"));
        mockMvc.perform(get("/api/v2/carts/" + cartId))
            .andExpect(jsonPath("$.items.length()").value(0));

        // 같은 캡처 재요청은 멱등
        mockMvc.perform(post("/api/v2/payments/capture").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\",\"paymentId\":\"" + PG_ORDER_ID + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void 다른_PayPal_주문ID로는_캡처할_수_없다() throws Exception {
        String orderId = placeOrder(createCartWithItems());
        mockMvc.perform(post("/api/v2/payments").contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":\"" + orderId + "\",\"paymentMethod\":\"PAYPAL\"}"));

        mockMvc.perform(post("/api/v2/payments/capture").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\",\"paymentId\":\"SOMEONE-ELSES-ORDER\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PAYMENT_MISMATCH"));
    }

    @Test
    void 캡처금액이_주문금액과_다르면_REVIEW_REQUIRED() throws Exception {
        String orderId = placeOrder(createCartWithItems());
        mockMvc.perform(post("/api/v2/payments").contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":\"" + orderId + "\",\"paymentMethod\":\"PAYPAL\"}"));

        given(paymentGateway.capture(any())).willReturn(PaymentResult.success(
            new TransactionInfo(PG_ORDER_ID, Money.usd("0.01"), "CAPTURE-X", null, orderId)));

        mockMvc.perform(post("/api/v2/payments/capture").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\",\"paymentId\":\"" + PG_ORDER_ID + "\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PAYMENT_AMOUNT_MISMATCH"));

        Payment payment = paymentRepository.findByOrderId(OrderId.of(orderId)).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REVIEW_REQUIRED);
        mockMvc.perform(get("/api/v2/orders/" + orderId)).andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void PayPal_팝업_취소시_주문이_취소되고_장바구니는_유지된다() throws Exception {
        String cartId = createCartWithItems();
        String orderId = placeOrder(cartId);
        mockMvc.perform(post("/api/v2/payments").contentType(MediaType.APPLICATION_JSON)
            .content("{\"orderId\":\"" + orderId + "\",\"paymentMethod\":\"PAYPAL\"}"));

        mockMvc.perform(post("/api/v2/payments/cancel/paypal").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":\"" + orderId + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v2/orders/" + orderId)).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(get("/api/v2/carts/" + cartId)).andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void 품절_상품은_담을_수_없다() throws Exception {
        String cartId = createCart();
        mockMvc.perform(post("/api/v2/carts/" + cartId + "/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1006,\"quantity\":1}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PRODUCT_NOT_PURCHASABLE"));
    }

    private String createCart() throws Exception {
        String body = mockMvc.perform(post("/api/v2/carts"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("cartId").asString();
    }

    private String createCartWithItems() throws Exception {
        String cartId = createCart();
        mockMvc.perform(post("/api/v2/carts/" + cartId + "/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1001,\"quantity\":2}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/v2/carts/" + cartId + "/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1003,\"quantity\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtotal.amount").value("52.90"))
            .andExpect(jsonPath("$.checkoutAvailable").value(true));
        return cartId;
    }

    private String placeOrder(String cartId) throws Exception {
        String request = """
            {
              "cartId": "%s",
              "email": "guest@example.com",
              "shippingAddress": {
                "fullName": "Jane Doe",
                "addressLine1": "1 Main St",
                "city": "San Jose",
                "state": "CA",
                "postalCode": "95131",
                "countryCode": "US"
              }
            }
            """.formatted(cartId);
        String body = mockMvc.perform(post("/api/v2/orders").contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("orderId").asString();
    }
}
