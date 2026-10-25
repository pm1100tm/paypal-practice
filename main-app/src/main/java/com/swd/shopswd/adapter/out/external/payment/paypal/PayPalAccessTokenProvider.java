package com.swd.shopswd.adapter.out.external.payment.paypal;

import com.swd.shopswd.adapter.out.external.payment.paypal.dto.PayPalAccessTokenResponse;
import com.swd.shopswd.domain.payment.exception.PaymentExternalException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * PayPal AccessToken 을 조회하되, 항상 "유효한 AccessToken"을 반환한다.
 *
 * <p>동작 방식:</p>
 * <ul>
 *     <li>메모리에 캐시된 토큰이 만료 전(안전 마진 {@link #EXPIRY_MARGIN})이면 그대로 사용한다.</li>
 *     <li>토큰이 없거나 만료된 경우, PayPal API 를 호출하여 새 토큰을 발급받아 캐시한다.</li>
 *     <li>API 호출이 401 을 받으면 {@link #invalidate()} 로 캐시를 비운다.</li>
 * </ul>
 *
 * <p>NOTE: 단일 인스턴스 메모리 캐시. 다중 인스턴스에서도 각자 발급하므로 정합성 문제는 없다.</p>
 */
@Slf4j
@Component
public class PayPalAccessTokenProvider {

    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(5);

    private final PayPalClient payPalClient;
    private final PayPalApiProperties properties;
    private final Clock clock;

    private volatile CachedToken cached;

    public PayPalAccessTokenProvider(PayPalClient payPalClient, PayPalApiProperties properties, Clock clock) {
        this.payPalClient = payPalClient;
        this.properties = properties;
        this.clock = clock;
    }

    public String getToken() {
        CachedToken current = cached;
        if (current != null && current.isValidAt(clock.instant())) {
            return current.token();
        }
        synchronized (this) {
            current = cached;
            if (current != null && current.isValidAt(clock.instant())) {
                return current.token();
            }
            cached = issue();
            return cached.token();
        }
    }

    public void invalidate() {
        cached = null;
    }

    private CachedToken issue() {
        if (!properties.hasCredentials()) {
            throw new PaymentExternalException("PAYPAL_NOT_CONFIGURED",
                "PayPal credentials are not configured (PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET)");
        }
        try {
            PayPalAccessTokenResponse response = payPalClient.fetchAccessToken();
            if (response == null || response.accessToken() == null) {
                throw new PaymentExternalException("PAYPAL_TOKEN_EMPTY", "PayPal access token response is empty");
            }
            Instant expiresAt = clock.instant().plusSeconds(response.expiresIn()).minus(EXPIRY_MARGIN);
            log.info("[PAY][PG:PayPal] access token issued");
            return new CachedToken(response.accessToken(), expiresAt);
        } catch (RestClientException e) {
            log.error("[PAY][PG:PayPal] failed to get accessToken: {}", e.getMessage());
            throw new PaymentExternalException("PAYPAL_TOKEN_FAILED", "Failed to obtain PayPal access token", e);
        }
    }

    private record CachedToken(String token, Instant expiresAt) {
        boolean isValidAt(Instant now) {
            return now.isBefore(expiresAt);
        }
    }
}
