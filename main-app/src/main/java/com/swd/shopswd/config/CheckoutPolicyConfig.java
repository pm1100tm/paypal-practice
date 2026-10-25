package com.swd.shopswd.config;

import com.swd.shopswd.domain.order.service.ShippingFeePolicy;
import com.swd.shopswd.domain.shared.value.Money;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 체크아웃 정책 Domain Service 빈 등록.
 */
@Configuration
public class CheckoutPolicyConfig {

    @Bean
    public ShippingFeePolicy shippingFeePolicy(
        @Value("${checkout.shipping.flat-fee:0.00}") BigDecimal flatFee,
        @Value("${checkout.shipping.free-threshold:}") String freeThreshold
    ) {
        Money threshold = StringUtils.hasText(freeThreshold) ? Money.usd(freeThreshold.trim()) : null;
        return new ShippingFeePolicy(Money.usd(flatFee), threshold);
    }
}
