package com.swd.shopswd.adapter.out.external.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

/**
 * PayPal Orders v2 — Create order 요청.
 * <a href="https://developer.paypal.com/docs/api/orders/v2/#orders_create">orders_create</a>
 *
 * NOTE: breakdown.item_total 과 Σ(items[].unit_amount × quantity) 가 다르면 ITEM_TOTAL_MISMATCH,
 *       amount.value 와 item_total + shipping 이 다르면 AMOUNT_MISMATCH 오류가 발생한다.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PayPalCreateOrderRequest(
    String intent,

    @JsonProperty("purchase_units")
    List<PurchaseUnit> purchaseUnits,

    @JsonProperty("payment_source")
    PaymentSource paymentSource
) {

    /* =========================
       Purchase Unit
       ========================= */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PurchaseUnit(
        @JsonProperty("reference_id") String referenceId,
        @JsonProperty("custom_id") String customId,
        @JsonProperty("invoice_id") String invoiceId,
        String description,
        Amount amount,
        List<Item> items,
        Shipping shipping
    ) {}

    /* =========================
       Amount / Breakdown
       ========================= */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Amount(
        @JsonProperty("currency_code") String currencyCode,
        String value,
        Breakdown breakdown
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Breakdown(
        @JsonProperty("item_total") PayPalMoney itemTotal,
        PayPalMoney shipping
    ) {}

    @Builder
    public record PayPalMoney(
        @JsonProperty("currency_code") String currencyCode,
        String value
    ) {}

    /* =========================
       Item
       ========================= */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
        String name,
        @JsonProperty("unit_amount") PayPalMoney unitAmount,
        String quantity,
        String sku,
        String category
    ) {}

    /* =========================
       Shipping
       ========================= */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Shipping(
        Name name,
        Address address
    ) {}

    @Builder
    public record Name(
        @JsonProperty("full_name") String fullName
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Address(
        @JsonProperty("address_line_1") String addressLine1,
        @JsonProperty("address_line_2") String addressLine2,
        @JsonProperty("admin_area_1") String adminArea1,
        @JsonProperty("admin_area_2") String adminArea2,
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("country_code") String countryCode
    ) {}

    /* =========================
       Payment Source (experience context)
       ========================= */
    @Builder
    public record PaymentSource(
        PayPal paypal
    ) {}

    @Builder
    public record PayPal(
        @JsonProperty("experience_context") ExperienceContext experienceContext
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ExperienceContext(
        @JsonProperty("brand_name") String brandName,
        @JsonProperty("shipping_preference") String shippingPreference,
        @JsonProperty("user_action") String userAction,
        @JsonProperty("return_url") String returnUrl,
        @JsonProperty("cancel_url") String cancelUrl
    ) {}
}
