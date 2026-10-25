package com.swd.shopswd.domain.order.value;

import java.util.Locale;
import java.util.Objects;

/**
 * 배송지 Value Object.
 * 필드는 PayPal Orders API 의 shipping.address 구조와 1:1 대응한다.
 *
 * @param fullName     수령인 이름
 * @param addressLine1 주소 1 (address_line_1)
 * @param addressLine2 주소 2 (address_line_2, optional)
 * @param city         도시 (admin_area_2)
 * @param state        주/도 (admin_area_1, optional — 국가에 따라 필수)
 * @param postalCode   우편번호 (optional — 국가에 따라 필수)
 * @param countryCode  ISO 3166-1 alpha-2 국가 코드
 */
public record ShippingAddress(
    String fullName,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String countryCode
) {

    public ShippingAddress {
        fullName = requireText(fullName, "fullName");
        addressLine1 = requireText(addressLine1, "addressLine1");
        city = requireText(city, "city");
        countryCode = requireText(countryCode, "countryCode").toUpperCase(Locale.ROOT);
        if (!countryCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("countryCode must be ISO 3166-1 alpha-2: " + countryCode);
        }
        addressLine2 = blankToNull(addressLine2);
        state = blankToNull(state);
        postalCode = blankToNull(postalCode);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
