package com.swd.shopswd.adapter.in.web.dto.shared;

import com.swd.shopswd.domain.shared.value.Money;

/**
 * 금액 응답. 부동소수점 오차를 피하기 위해 amount 는 문자열로 내려준다 (예: "12.30").
 */
public record MoneyResponse(String amount, String currency) {

    public static MoneyResponse from(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyResponse(money.toPlainString(), money.currencyCode());
    }
}
