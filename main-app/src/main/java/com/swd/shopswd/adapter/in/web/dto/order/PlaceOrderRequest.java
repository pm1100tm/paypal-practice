package com.swd.shopswd.adapter.in.web.dto.order;

import com.swd.shopswd.application.port.in.order.PlaceOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 비회원 주문 생성 요청.
 * 금액 관련 필드는 받지 않는다 — 서버가 장바구니 기준으로 계산한다.
 */
public record PlaceOrderRequest(
    @NotBlank String cartId,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 50) String phone,
    @NotNull @Valid ShippingAddressRequest shippingAddress
) {

    public record ShippingAddressRequest(
        @NotBlank @Size(max = 300) String fullName,
        @NotBlank @Size(max = 300) String addressLine1,
        @Size(max = 300) String addressLine2,
        @NotBlank @Size(max = 120) String city,
        @Size(max = 300) String state,
        @Size(max = 60) String postalCode,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be ISO 3166-1 alpha-2") String countryCode
    ) {}

    public PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(
            cartId,
            email,
            phone,
            shippingAddress.fullName(),
            shippingAddress.addressLine1(),
            shippingAddress.addressLine2(),
            shippingAddress.city(),
            shippingAddress.state(),
            shippingAddress.postalCode(),
            shippingAddress.countryCode()
        );
    }
}
