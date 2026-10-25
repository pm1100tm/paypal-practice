package com.swd.shopswd.application.service.order;

import com.swd.shopswd.application.port.in.order.CheckoutSummaryResult;
import com.swd.shopswd.application.port.in.order.OrderLineResult;
import com.swd.shopswd.application.port.in.order.PlaceOrderCommand;
import com.swd.shopswd.application.port.in.order.PlaceOrderResult;
import com.swd.shopswd.application.port.in.order.PlaceOrderUseCase;
import com.swd.shopswd.application.port.in.order.PreviewCheckoutUseCase;
import com.swd.shopswd.domain.cart.aggregate.Cart;
import com.swd.shopswd.domain.cart.entity.CartItem;
import com.swd.shopswd.domain.cart.exception.CartNotFoundException;
import com.swd.shopswd.domain.cart.repository.CartRepository;
import com.swd.shopswd.domain.cart.value.CartId;
import com.swd.shopswd.domain.order.aggregate.Order;
import com.swd.shopswd.domain.order.entity.OrderItem;
import com.swd.shopswd.domain.order.exception.EmptyOrderException;
import com.swd.shopswd.domain.order.repository.OrderRepository;
import com.swd.shopswd.domain.order.service.ShippingFeePolicy;
import com.swd.shopswd.domain.order.value.OrderContact;
import com.swd.shopswd.domain.order.value.ShippingAddress;
import com.swd.shopswd.domain.product.aggregate.Product;
import com.swd.shopswd.domain.product.exception.ProductNotFoundException;
import com.swd.shopswd.domain.product.repository.ProductRepository;
import com.swd.shopswd.domain.product.value.ProductId;
import com.swd.shopswd.domain.shared.value.Money;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 체크아웃 유스케이스 — 금액 미리보기 / 주문 생성.
 * 금액은 클라이언트 값을 신뢰하지 않고 항상 장바구니 + 현재 상품가로 재계산한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService implements PreviewCheckoutUseCase, PlaceOrderUseCase {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ShippingFeePolicy shippingFeePolicy;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public CheckoutSummaryResult preview(String cartId) {
        Cart cart = loadCart(cartId);
        List<OrderItem> lines = buildOrderLines(cart);

        Money itemTotal = lines.stream().map(OrderItem::lineTotal).reduce(Money::plus).orElseThrow();
        Money shippingCost = shippingFeePolicy.calculate(itemTotal);

        return new CheckoutSummaryResult(
            cart.getId().value(),
            lines.stream().map(OrderLineResult::from).toList(),
            itemTotal,
            shippingCost,
            itemTotal.plus(shippingCost),
            shippingFeePolicy.freeShippingThreshold().orElse(null)
        );
    }

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        Cart cart = loadCart(command.cartId());
        List<OrderItem> lines = buildOrderLines(cart);

        Money itemTotal = lines.stream().map(OrderItem::lineTotal).reduce(Money::plus).orElseThrow();
        Money shippingCost = shippingFeePolicy.calculate(itemTotal);

        OrderContact contact = new OrderContact(command.email(), command.phone());
        ShippingAddress address = new ShippingAddress(
            command.fullName(),
            command.addressLine1(),
            command.addressLine2(),
            command.city(),
            command.state(),
            command.postalCode(),
            command.countryCode()
        );

        Order order = orderRepository.save(
            Order.place(cart.getId(), lines, shippingCost, contact, address, clock.instant()));

        log.info("[ORDER][Place] orderId={} | cartId={} | total={}",
            order.getId().value(), cart.getId().value(), order.getTotalAmount());
        return new PlaceOrderResult(order.getId().value(), order.getTotalAmount());
    }

    private Cart loadCart(String cartId) {
        CartId id = CartId.of(cartId);
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new CartNotFoundException(id));
        if (cart.isEmpty()) {
            throw new EmptyOrderException();
        }
        return cart;
    }

    /** 장바구니 라인을 현재 상품가 기준 주문 라인 스냅샷으로 변환 */
    private List<OrderItem> buildOrderLines(Cart cart) {
        Map<ProductId, Product> products = productRepository.findAllByIds(cart.productIds());
        return cart.getItems().stream()
            .map(item -> toOrderItem(item, products))
            .toList();
    }

    private OrderItem toOrderItem(CartItem item, Map<ProductId, Product> products) {
        Product product = products.get(item.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(item.getProductId());
        }
        product.ensurePurchasable();
        return new OrderItem(product.getId(), product.getName(), product.getPrice(), item.getQuantity());
    }
}
