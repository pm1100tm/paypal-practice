package com.swd.shopswd.adapter.in.web.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
    @NotNull @Positive Long productId,
    @Min(1) @Max(99) int quantity
) {}
