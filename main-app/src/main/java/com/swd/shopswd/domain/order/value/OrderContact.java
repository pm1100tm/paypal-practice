package com.swd.shopswd.domain.order.value;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 비회원 주문자 연락처.
 */
public record OrderContact(String email, String phone) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public OrderContact {
        Objects.requireNonNull(email, "email must not be null");
        email = email.trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        phone = phone == null || phone.isBlank() ? null : phone.trim();
    }
}
