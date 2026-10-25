package com.swd.shopswd.adapter.out.external.payment.paypal;

public final class PayPalConst {

    private PayPalConst() {}

    /* PayPal Default Currency - v1 은 USD 단일 통화 */
    public static final String DEFAULT_CURRENCY = "USD";

    /* PATH Constants */
    public static final String PATH_ACCESS_TOKEN = "/v1/oauth2/token";
    public static final String PATH_CHECKOUT_ORDER = "/v2/checkout/orders";
    public static final String PATH_ORDER_DETAIL = "/v2/checkout/orders/{pgOrderId}";
    public static final String PATH_CAPTURE_ORDER = "/v2/checkout/orders/{pgOrderId}/capture";

    /* DOMAIN Constants */
    public static final String INTENT_CAPTURE = "CAPTURE";
    public static final String SHIPPING_PREFERENCE = "SET_PROVIDED_ADDRESS";
    public static final String USER_ACTION_PAY_NOW = "PAY_NOW";
    public static final String ITEM_CATEGORY_PHYSICAL = "PHYSICAL_GOODS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    /* HEADER Constants */
    public static final String REQUEST_ID = "PayPal-Request-Id";
    public static final String PREFER = "Prefer";
    public static final String PREFER_REPRESENTATION = "return=representation";

    /* ERROR issue Constants */
    public static final String ISSUE_ORDER_ALREADY_CAPTURED = "ORDER_ALREADY_CAPTURED";
    public static final String ISSUE_INSTRUMENT_DECLINED = "INSTRUMENT_DECLINED";
    public static final String ISSUE_PAYER_ACTION_REQUIRED = "PAYER_ACTION_REQUIRED";

    /* Field length limits (PayPal Orders v2) */
    public static final int MAX_ITEM_NAME = 127;
    public static final int MAX_FULL_NAME = 300;
    public static final int MAX_ADDRESS_LINE = 300;
    public static final int MAX_ADMIN_AREA_2 = 120;
    public static final int MAX_ADMIN_AREA_1 = 300;
    public static final int MAX_POSTAL_CODE = 60;

    /**
     * PayPal 중복 처리 방지용 멱등성 키.
     * 같은 키로 재요청하면 PayPal 은 최초 응답을 그대로 반환한다 (재시도 안전).
     */
    public static String requestIdForCreate(String paymentId) {
        return paymentId + "-create";
    }

    public static String requestIdForCapture(String pgOrderId) {
        return pgOrderId + "-capture";
    }
}
