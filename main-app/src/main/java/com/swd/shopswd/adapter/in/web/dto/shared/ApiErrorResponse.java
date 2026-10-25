package com.swd.shopswd.adapter.in.web.dto.shared;

import java.util.List;

public record ApiErrorResponse(String code, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {}

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, List.of());
    }
}
