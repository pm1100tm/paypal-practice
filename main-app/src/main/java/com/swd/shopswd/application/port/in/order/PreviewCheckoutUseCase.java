package com.swd.shopswd.application.port.in.order;

public interface PreviewCheckoutUseCase {

    CheckoutSummaryResult preview(String cartId);
}
