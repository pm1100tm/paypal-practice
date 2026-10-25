package com.swd.shopswd.domain.payment.value;

public enum PaymentStatus {
    /** 결제 엔티티 생성, PG 주문 생성 전 */
    READY,
    /** PG 주문 생성 완료, 구매자 승인 대기 */
    PENDING_APPROVAL,
    /** 캡처 완료 */
    COMPLETED,
    /** PG 거절 (결제수단 문제) */
    DECLINED,
    /** 캡처 전 실패 */
    FAILED,
    /** 사용자 취소 */
    CANCELLED,
    /** 캡처 이후 검증 실패 등 — 수동 확인 필요 (금액 불일치, 응답 누락 등) */
    REVIEW_REQUIRED;

    public boolean isTerminal() {
        return this != READY && this != PENDING_APPROVAL;
    }
}
