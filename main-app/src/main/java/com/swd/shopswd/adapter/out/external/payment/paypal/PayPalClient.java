package com.swd.shopswd.adapter.out.external.payment.paypal;

import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalAccessTokenResponse;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalCreateOrderRequest;
import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalOrderResponse;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * PayPal Orders API 호출 전담 클라이언트.
 *
 * <p>5xx 서버 오류 및 네트워크 장애에 대해 재시도를 수행한다 (PayPal-Request-Id 멱등키로 중복 처리 방지).</p>
 * <p>4xx 클라이언트 오류는 retry 없이 즉시 호출부로 전파된다.</p>
 * <p>NOTE: 요청/응답 본문에는 배송지·이메일 등 개인정보가 포함되므로 로그에 남기지 않는다.</p>
 */
@Slf4j
@Component
public class PayPalClient {

    private final RestClient restClient;
    private final PayPalApiProperties properties;

    public PayPalClient(PayPalApiProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.getConnectTimeout())
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    private String encodeBasicCredentials() {
        String input = properties.getClientId() + ":" + properties.getClientSecret();
        return "Basic " + Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * PayPal Access Token 발급 API를 호출한다.
     * - <a href="https://developer.paypal.com/api/rest/authentication/"></a>
     *
     * <p>재시도 정책: 5xx / 네트워크 오류, 최대 3회 (초기 1회 + 재시도 2회), 백오프 1초 → 2초</p>
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        maxRetries = 2,
        delay = 1000,
        multiplier = 2
    )
    public @Nullable PayPalAccessTokenResponse fetchAccessToken() {
        log.info("[PayPalClient] fetchAccessToken 요청");
        return restClient.post()
            .uri(PayPalConst.PATH_ACCESS_TOKEN)
            .headers(headers -> {
                headers.set(HttpHeaders.AUTHORIZATION, encodeBasicCredentials());
                headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en_US");
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            })
            .body("grant_type=client_credentials")
            .retrieve()
            .body(PayPalAccessTokenResponse.class);
    }

    /**
     * PayPal /v2/checkout/orders API를 호출한다.
     * - PayPal Order 생성
     * - <a href="https://developer.paypal.com/docs/api/orders/v2/#orders_create"></a>
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        maxRetries = 2,
        delay = 1000,
        multiplier = 2
    )
    public @Nullable PayPalOrderResponse createOrder(String accessToken, PayPalCreateOrderRequest body,
                                                     String requestId) {
        log.info("[PayPalClient] createOrder 요청: requestId={}", requestId);
        PayPalOrderResponse response = restClient.post()
            .uri(PayPalConst.PATH_CHECKOUT_ORDER)
            .headers(headers -> {
                headers.setBearerAuth(accessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set(PayPalConst.REQUEST_ID, requestId);
            })
            .body(body)
            .retrieve()
            .body(PayPalOrderResponse.class);

        log.info("[PayPalClient] createOrder 응답: requestId={}, pgOrderId={}, status={}",
            requestId,
            response != null ? response.id() : null,
            response != null ? response.status() : null);
        return response;
    }

    /**
     * PayPal /v2/checkout/orders/{id}/capture API를 호출한다.
     * - PayPal Order 확정
     * - <a href="https://developer.paypal.com/docs/api/orders/v2/#orders_capture"></a>
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        maxRetries = 2,
        delay = 1000,
        multiplier = 2
    )
    public @Nullable PayPalOrderResponse captureOrder(String accessToken, String pgOrderId) {
        log.info("[PayPalClient] captureOrder 요청: pgOrderId={}", pgOrderId);
        PayPalOrderResponse response = restClient.post()
            .uri(PayPalConst.PATH_CAPTURE_ORDER, pgOrderId)
            .headers(headers -> {
                headers.setBearerAuth(accessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set(PayPalConst.REQUEST_ID, PayPalConst.requestIdForCapture(pgOrderId));
                headers.set(PayPalConst.PREFER, PayPalConst.PREFER_REPRESENTATION);
            })
            .body("{}")
            .retrieve()
            .body(PayPalOrderResponse.class);

        log.info("[PayPalClient] captureOrder 응답: pgOrderId={}, status={}",
            pgOrderId, response != null ? response.status() : null);
        return response;
    }

    /**
     * PayPal /v2/checkout/orders/{id} API를 호출한다.
     * - PayPal Order Detail 정보 취득
     * - <a href="https://developer.paypal.com/docs/api/orders/v2/#orders_get"></a>
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        maxRetries = 2,
        delay = 1000,
        multiplier = 2
    )
    public @Nullable PayPalOrderResponse getOrderDetail(String accessToken, String pgOrderId) {
        log.info("[PayPalClient] getOrderDetail 요청: pgOrderId={}", pgOrderId);
        PayPalOrderResponse response = restClient.get()
            .uri(PayPalConst.PATH_ORDER_DETAIL, pgOrderId)
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(PayPalOrderResponse.class);

        log.info("[PayPalClient] getOrderDetail 응답: pgOrderId={}, status={}",
            pgOrderId, response != null ? response.status() : null);
        return response;
    }
}
