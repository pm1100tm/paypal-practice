package com.swd.shopswd.domain.cart.value;

import java.util.Objects;
import java.util.UUID;

/**
 * 비회원 장바구니 식별자.
 *
 * 로그인/세션이 없으므로 추측 불가능한 랜덤 UUID 자체가 장바구니 접근 권한 역할을 한다.
 * (클라이언트는 이 값을 localStorage 에 보관)
 */
public record CartId(String value) {

    public CartId {
        Objects.requireNonNull(value, "CartId must not be null");
        try {
            value = UUID.fromString(value).toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid CartId format: " + value, e);
        }
    }

    public static CartId of(String value) {
        return new CartId(value);
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID().toString());
    }
}
