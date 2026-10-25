package com.swd.shopswd.adapter.in.web.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ChangeCartItemQuantityRequest(
    @Min(1) @Max(99) int quantity
) {}
