package com.swd.shopswd.adapter.out.external.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * PayPal Orders v2 — Order 리소스.
 * Create / Capture / Get 응답이 모두 같은 Order 스키마를 따르므로 하나의 DTO 로 받는다.
 * (Capture 는 Prefer: return=representation 헤더로 전체 표현을 요청)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PayPalOrderResponse(
    String id,
    String status,
    List<Link> links,
    @JsonProperty("purchase_units") List<PurchaseUnit> purchaseUnits
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PurchaseUnit(
        @JsonProperty("reference_id") String referenceId,
        @JsonProperty("custom_id") String customId,
        @JsonProperty("invoice_id") String invoiceId,
        Amount amount,
        Payments payments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payments(List<Capture> captures) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Capture(
        String id,
        String status,
        Amount amount,
        @JsonProperty("custom_id") String customId,
        @JsonProperty("invoice_id") String invoiceId,
        @JsonProperty("status_details") StatusDetails statusDetails,
        @JsonProperty("seller_receivable_breakdown") SellerReceivableBreakdown sellerReceivableBreakdown
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusDetails(String reason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SellerReceivableBreakdown(
        @JsonProperty("gross_amount") Amount grossAmount,
        @JsonProperty("paypal_fee") Amount paypalFee,
        @JsonProperty("net_amount") Amount netAmount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(
        @JsonProperty("currency_code") String currencyCode,
        String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Link(String href, String rel, String method) {}

    /** PayPal 결제 승인 페이지 URL (redirect 용) */
    public Optional<String> approveUrl() {
        if (links == null) {
            return Optional.empty();
        }
        return links.stream()
            .filter(link -> "approve".equals(link.rel()) || "payer-action".equals(link.rel()))
            .map(Link::href)
            .findFirst();
    }

    public Optional<PurchaseUnit> firstPurchaseUnit() {
        return purchaseUnits == null ? Optional.empty() : purchaseUnits.stream().findFirst();
    }

    public Optional<Capture> firstCapture() {
        return firstPurchaseUnit()
            .map(PurchaseUnit::payments)
            .map(Payments::captures)
            .flatMap(captures -> captures.stream().findFirst());
    }
}
