package com.swd.shopswd.adapter.in.web.api;

import com.swd.shopswd.adapter.in.web.dto.cart.AddCartItemRequest;
import com.swd.shopswd.adapter.in.web.dto.cart.CartResponse;
import com.swd.shopswd.adapter.in.web.dto.cart.ChangeCartItemQuantityRequest;
import com.swd.shopswd.application.port.in.cart.AddCartItemCommand;
import com.swd.shopswd.application.port.in.cart.AddCartItemUseCase;
import com.swd.shopswd.application.port.in.cart.ChangeCartItemQuantityCommand;
import com.swd.shopswd.application.port.in.cart.ChangeCartItemQuantityUseCase;
import com.swd.shopswd.application.port.in.cart.CreateCartUseCase;
import com.swd.shopswd.application.port.in.cart.GetCartUseCase;
import com.swd.shopswd.application.port.in.cart.RemoveCartItemUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 비회원 Cart Controller.
 *
 * <p>로그인/세션 없이 cartId(UUID) 로 장바구니를 식별한다.
 * 클라이언트는 POST /api/v2/carts 로 발급받은 cartId 를 보관해 이후 요청에 사용한다.</p>
 */
@RestController
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
public class CartController {

    private final CreateCartUseCase createCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final ChangeCartItemQuantityUseCase changeCartItemQuantityUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse createCart() {
        return CartResponse.from(createCartUseCase.createCart());
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable String cartId) {
        return CartResponse.from(getCartUseCase.getCart(cartId));
    }

    @PostMapping("/{cartId}/items")
    public CartResponse addItem(@PathVariable String cartId, @Valid @RequestBody AddCartItemRequest request) {
        return CartResponse.from(addCartItemUseCase.addItem(
            new AddCartItemCommand(cartId, request.productId(), request.quantity())));
    }

    @PatchMapping("/{cartId}/items/{productId}")
    public CartResponse changeQuantity(@PathVariable String cartId,
                                       @PathVariable Long productId,
                                       @Valid @RequestBody ChangeCartItemQuantityRequest request) {
        return CartResponse.from(changeCartItemQuantityUseCase.changeQuantity(
            new ChangeCartItemQuantityCommand(cartId, productId, request.quantity())));
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public CartResponse removeItem(@PathVariable String cartId, @PathVariable Long productId) {
        return CartResponse.from(removeCartItemUseCase.removeItem(cartId, productId));
    }
}
