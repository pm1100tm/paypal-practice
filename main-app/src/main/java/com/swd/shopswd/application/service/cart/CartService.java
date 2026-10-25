package com.swd.shopswd.application.service.cart;

import com.swd.shopswd.application.port.in.cart.AddCartItemCommand;
import com.swd.shopswd.application.port.in.cart.AddCartItemUseCase;
import com.swd.shopswd.application.port.in.cart.CartItemResult;
import com.swd.shopswd.application.port.in.cart.CartResult;
import com.swd.shopswd.application.port.in.cart.ChangeCartItemQuantityCommand;
import com.swd.shopswd.application.port.in.cart.ChangeCartItemQuantityUseCase;
import com.swd.shopswd.application.port.in.cart.CreateCartUseCase;
import com.swd.shopswd.application.port.in.cart.GetCartUseCase;
import com.swd.shopswd.application.port.in.cart.RemoveCartItemUseCase;
import com.swd.shopswd.domain.cart.aggregate.Cart;
import com.swd.shopswd.domain.cart.entity.CartItem;
import com.swd.shopswd.domain.cart.exception.CartNotFoundException;
import com.swd.shopswd.domain.cart.repository.CartRepository;
import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.product.aggregate.Product;
import com.swd.shopswd.domain.product.exception.ProductNotFoundException;
import com.swd.shopswd.domain.product.repository.ProductRepository;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비회원 장바구니 유스케이스.
 * 세션/로그인 없이 cartId(UUID) 하나로 장바구니를 식별한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements CreateCartUseCase, GetCartUseCase, AddCartItemUseCase,
    ChangeCartItemQuantityUseCase, RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Override
    public CartResult createCart() {
        Cart cart = cartRepository.save(Cart.create(now()));
        return toResult(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResult getCart(String cartId) {
        return toResult(loadCart(cartId));
    }

    @Override
    public CartResult addItem(AddCartItemCommand command) {
        Cart cart = loadCart(command.cartId());
        ProductId productId = ProductId.of(command.productId());
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.ensurePurchasable();

        cart.addItem(productId, command.quantity(), now());
        return toResult(cartRepository.save(cart));
    }

    @Override
    public CartResult changeQuantity(ChangeCartItemQuantityCommand command) {
        Cart cart = loadCart(command.cartId());
        cart.changeQuantity(ProductId.of(command.productId()), command.quantity(), now());
        return toResult(cartRepository.save(cart));
    }

    @Override
    public CartResult removeItem(String cartId, Long productId) {
        Cart cart = loadCart(cartId);
        cart.removeItem(ProductId.of(productId), now());
        return toResult(cartRepository.save(cart));
    }

    private Cart loadCart(String cartId) {
        CartId id = CartId.of(cartId);
        return cartRepository.findById(id).orElseThrow(() -> new CartNotFoundException(id));
    }

    /**
     * 현재 상품 가격 기준으로 장바구니 뷰를 구성한다.
     * 삭제된 상품은 뷰에서 제외하고, 판매 중지 상품은 purchasable=false 로 노출한다.
     */
    private CartResult toResult(Cart cart) {
        Map<ProductId, Product> products = productRepository.findAllByIds(cart.productIds());

        List<CartItemResult> items = new ArrayList<>();
        Money subtotal = Money.zero(Money.USD);
        int totalQuantity = 0;
        boolean allPurchasable = true;

        for (CartItem item : cart.getItems()) {
            Product product = products.get(item.getProductId());
            if (product == null) {
                allPurchasable = false;
                continue;
            }
            Money lineTotal = product.getPrice().times(item.getQuantity());
            items.add(new CartItemResult(
                product.getId().value(),
                product.getName(),
                product.getBrand(),
                product.getImageUrl(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal,
                product.isPurchasable()
            ));
            if (product.isPurchasable()) {
                subtotal = subtotal.plus(lineTotal);
                totalQuantity += item.getQuantity();
            } else {
                allPurchasable = false;
            }
        }

        return new CartResult(cart.getId().value(), items, totalQuantity, subtotal,
            !items.isEmpty() && allPurchasable);
    }

    private Instant now() {
        return clock.instant();
    }
}
