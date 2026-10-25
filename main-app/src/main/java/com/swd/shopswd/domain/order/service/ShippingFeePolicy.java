package com.swd.shopswd.domain.order.service;

import com.swd.shopswd.domain.shared.value.Money;
import java.util.Objects;
import java.util.Optional;

/**
 * 배송비 정책 (Domain Service).
 *
 * v1 은 단일 정액 배송비 + (선택) 무료배송 기준금액만 지원한다.
 * 국가별/중량별 배송비, 세금(관부가세) 계산은 범위 밖이다.
 */
public class ShippingFeePolicy {

    private final Money flatFee;
    private final Money freeShippingThreshold; // nullable

    public ShippingFeePolicy(Money flatFee, Money freeShippingThreshold) {
        this.flatFee = Objects.requireNonNull(flatFee);
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public Money calculate(Money itemTotal) {
        if (freeShippingThreshold != null && itemTotal.isGreaterThanOrEqual(freeShippingThreshold)) {
            return Money.zero(itemTotal.currency());
        }
        return flatFee;
    }

    public Optional<Money> freeShippingThreshold() {
        return Optional.ofNullable(freeShippingThreshold);
    }
}
