package com.swd.shopswd.application.port.in.order;

public record PlaceOrderCommand(
    String cartId,
    String email,
    String phone,
    String fullName,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String countryCode
) {}
