package com.swd.shopswd.domain.order.value;

public enum OrderStatus {
    /** 주문 생성, 결제 대기 */
    PENDING_PAYMENT,
    /** 결제 완료 */
    PAID,
    /** 결제 전 취소 (사용자 취소, PG 거절/오류 등) */
    CANCELLED
}
