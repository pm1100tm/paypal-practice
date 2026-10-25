package com.swd.shopswd.adapter.in.web.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CancelPaypalRequest(@NotBlank String orderId) {}
