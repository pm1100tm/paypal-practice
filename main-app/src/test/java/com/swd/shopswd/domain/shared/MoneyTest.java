package com.swd.shopswd.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.swd.shopswd.domain.shared.value.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void USD_는_소수_둘째자리로_정규화된다() {
        assertThat(Money.usd("10.005").toPlainString()).isEqualTo("10.01");
        assertThat(Money.usd(new BigDecimal("3")).toPlainString()).isEqualTo("3.00");
    }

    @Test
    void 덧셈과_곱셈() {
        Money result = Money.usd("18.50").times(2).plus(Money.usd("15.90"));
        assertThat(result).isEqualTo(Money.usd("52.90"));
    }

    @Test
    void 음수는_허용하지_않는다() {
        assertThatThrownBy(() -> Money.usd("-1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 통화가_다르면_연산할_수_없다() {
        assertThatThrownBy(() -> Money.usd("1").plus(Money.of(BigDecimal.ONE, "EUR")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
