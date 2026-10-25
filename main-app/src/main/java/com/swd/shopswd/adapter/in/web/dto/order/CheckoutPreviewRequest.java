package com.swd.shopswd.adapter.in.web.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CheckoutPreviewRequest(@NotBlank String cartId) {}
