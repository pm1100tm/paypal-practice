package com.swd.shopswd.domain.shared.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object representing monetary amount.
 *
 * 특징:
 * - 순수 Value Object (외부 의존성 없음)
 * - 금액 + 통화 표현, 통화별 소수 자릿수로 정규화 (USD → 2자리)
 * - 같은 통화 간 산술 연산 및 비교만 허용
 *
 * NOTE: v1 은 PayPal 결제만 지원하므로 USD 단일 통화로 운영한다.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final Currency USD = Currency.getInstance("USD");

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative: " + amount);
        }
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }

    // ============================================
    // Factory Methods
    // ============================================

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, USD);
    }

    public static Money usd(String amount) {
        return usd(new BigDecimal(amount));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // ============================================
    // Operations
    // ============================================

    public Money plus(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must not be negative: " + quantity);
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return compareTo(other) >= 0;
    }

    public String currencyCode() {
        return currency.getCurrencyCode();
    }

    /** PayPal 등 외부 API 전송용 문자열 (예: "12.30") */
    public String toPlainString() {
        return amount.toPlainString();
    }

    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void assertSameCurrency(Money other) {
        Objects.requireNonNull(other, "Other money must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
