package com.swd.shopswd.domain.payment.value;

import com.swd.shopswd.domain.shared.value.Money;
import java.util.Objects;

/**
 * PG 캡처 결과.
 *
 * @param pgOrderId      PG 주문 ID (PayPal Order ID)
 * @param capturedAmount 실제 캡처 금액
 * @param captureId      PG 거래 ID (PayPal Capture ID)
 * @param pgFee          PG 수수료 (optional)
 * @param customId       PG 에 전달했던 가맹점 주문 ID (optional, 검증용)
 */
public record TransactionInfo(
    String pgOrderId,
    Money capturedAmount,
    String captureId,
    Money pgFee,
    String customId
) {

    public TransactionInfo {
        Objects.requireNonNull(pgOrderId, "pgOrderId must not be null");
        Objects.requireNonNull(capturedAmount, "capturedAmount must not be null");
        Objects.requireNonNull(captureId, "captureId must not be null");
    }
}
