package com.swd.shopswd.domain.order.value;

public enum CancelReason {
    /** 사용자가 PayPal 팝업에서 취소 */
    USER_CANCELLED,
    /** 클라이언트(PayPal SDK) 오류 */
    CLIENT_ERROR,
    /** PG 결제 거절 (INSTRUMENT_DECLINED 등) */
    PG_DECLINED,
    /** PG 오류 */
    PG_ERROR,
    /** PG 주문 생성 실패 */
    CREATE_PAYMENT_FAILED
}
