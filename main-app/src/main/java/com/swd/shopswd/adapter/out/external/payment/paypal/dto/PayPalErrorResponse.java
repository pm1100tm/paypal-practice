package com.swd.shopswd.adapter.out.external.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * PayPal API 에러 응답.
 * <a href="https://developer.paypal.com/api/rest/responses/#link-errorresponses">Error responses</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PayPalErrorResponse(
    String name,
    String message,
    @JsonProperty("debug_id") String debugId,
    List<Detail> details
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detail(String field, String issue, String description) {}

    public String firstIssue() {
        if (details == null || details.isEmpty()) {
            return null;
        }
        return details.getFirst().issue();
    }
}
