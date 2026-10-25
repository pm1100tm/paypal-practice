package com.swd.shopswd.adapter.in.web.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ErrorPaypalRequest(
    @NotBlank String orderId,
    @Size(max = 200) String errorName,
    @Size(max = 1000) String errorMessage
) {}
