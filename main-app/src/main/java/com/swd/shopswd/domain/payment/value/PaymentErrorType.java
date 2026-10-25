package com.swd.shopswd.domain.payment.value;

/**
 * 결제 실패 시점 구분.
 */
public enum PaymentErrorType {
    /** 캡처 전 실패 — 금전 이동 없음, 주문 취소 가능 */
    BEFORE_CAPTURE,
    /** 캡처 이후(또는 캡처 여부 불확실) 실패 — 수동 확인 필요 */
    AFTER_CAPTURE
}
