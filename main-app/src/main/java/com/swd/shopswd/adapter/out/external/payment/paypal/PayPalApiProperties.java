package com.swd.shopswd.adapter.out.external.payment.paypal;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PayPal API configuration properties.
 * PayPal API 설정 (application.properties / 환경변수에서 로드)
 *
 * <p>clientId / clientSecret 은 저장소에 커밋하지 않는다 — 환경변수 PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET 로 주입.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.paypal")
public class PayPalApiProperties {

    /** Sandbox: https://api-m.sandbox.paypal.com / Live: https://api-m.paypal.com */
    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private String clientId;
    private String clientSecret;

    /** PayPal 결제 화면에 노출되는 상호 */
    private String brandName = "ShopSWD";

    /**
     * experience_context 의 return_url / cancel_url.
     * JS SDK 팝업 흐름에서는 사용되지 않지만, 리다이렉트 흐름 대비용으로 프론트엔드 주소를 넣는다.
     */
    private String returnUrl;
    private String cancelUrl;

    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(20);

    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank()
            && clientSecret != null && !clientSecret.isBlank();
    }
}
