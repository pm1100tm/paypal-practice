package com.swd.shopswd.application.port.in.payment;

import com.swd.shopswd.domain.order.value.CancelReason;

public interface CancelPaymentUseCase {

    /**
     * 결제 전 취소 (PayPal 팝업 취소 / SDK 오류).
     * 장바구니는 결제 완료 전까지 비우지 않으므로 별도 복원이 필요 없다.
     */
    void cancelBeforePayment(String orderId, CancelReason reason);
}
