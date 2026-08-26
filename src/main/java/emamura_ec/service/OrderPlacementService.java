package emamura_ec.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.dto.CheckoutOrderItemData;
import emamura_ec.dto.CheckoutOrderSnapshot;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.Order;
import emamura_ec.entity.OrderAddress;
import emamura_ec.entity.OrderItem;
import emamura_ec.entity.OrderStatus;
import emamura_ec.entity.PaymentMethod;
import emamura_ec.entity.Product;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.User;
import emamura_ec.entity.WrappingType;
import emamura_ec.exception.CheckoutConfirmException;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;
import emamura_ec.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class OrderPlacementService {

    private static final String CART_SESSION_ATTRIBUTE = "cartItems";
    private static final String CHECKOUT_GIFT_ENABLED_SESSION_ATTRIBUTE = "checkoutGiftEnabled";
    private static final String CHECKOUT_GIFT_SESSION_ATTRIBUTE = "checkoutGift";
    private static final String CHECKOUT_DELIVERY_SESSION_ATTRIBUTE = "checkoutDeliveryData";
    private static final String CART_REDIRECT = "/cart";

    /*
     * Payment selection is not part of the current checkout flow, while the existing orders schema requires a value.
     * Keep this as an explicit temporary choice and replace it when the payment selection flow is implemented.
     */
    private static final PaymentMethod DEFAULT_PAYMENT_METHOD = PaymentMethod.CASH_ON_DELIVERY;

    private final CheckoutConfirmService checkoutConfirmService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;

    public OrderPlacementService(
            CheckoutConfirmService checkoutConfirmService,
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderAddressRepository orderAddressRepository) {
        this.checkoutConfirmService = checkoutConfirmService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
    }

    @Transactional
    public Long placeOrder(HttpSession session, String loginEmail) {
        // This rebuilds the checkout snapshot at POST time; the old confirmation HTML is never trusted for price or stock.
        CheckoutOrderSnapshot snapshot = checkoutConfirmService.createSnapshot(session);
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "ログインユーザーを確認できません。", CART_REDIRECT));

        Order order = new Order(
                user,
                LocalDateTime.now(),
                OrderStatus.PENDING,
                toOrderAmount(snapshot.getProductSubtotal()),
                toOrderAmount(snapshot.getShippingFee()),
                snapshot.getPaperBagCount(),
                snapshot.getPaperBagUnitPrice(),
                toOrderAmount(snapshot.getTotal()),
                snapshot.getDeliveryData().getDeliveryMethod(),
                null,
                DEFAULT_PAYMENT_METHOD);

        // Save the parent first so its generated orderId can be used by child rows without relying on cascade settings.
        Order savedOrder = orderRepository.saveAndFlush(order);

        List<OrderItem> orderItems = snapshot.getItems().stream()
                .map(item -> toOrderItem(savedOrder, item))
                .toList();
        orderItemRepository.saveAll(orderItems);

        CheckoutDeliveryData deliveryData = snapshot.getDeliveryData();
        if (deliveryData.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP) {
            orderAddressRepository.save(toOrderAddress(savedOrder, deliveryData));
        }

        decreaseStock(snapshot);
        return savedOrder.getOrderId();
    }

    /**
     * Called by the controller only after the transactional placement method has returned successfully.
     * The login Session remains intact; only data belonging to this completed checkout is removed.
     */
    public void clearCheckoutSession(HttpSession session) {
        session.removeAttribute(CART_SESSION_ATTRIBUTE);
        session.removeAttribute(CHECKOUT_GIFT_ENABLED_SESSION_ATTRIBUTE);
        session.removeAttribute(CHECKOUT_GIFT_SESSION_ATTRIBUTE);
        session.removeAttribute(CHECKOUT_DELIVERY_SESSION_ATTRIBUTE);
    }

    private OrderItem toOrderItem(Order order, CheckoutOrderItemData item) {
        return new OrderItem(
                order,
                item.getProduct(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getWrappingType() == null ? WrappingType.NONE : item.getWrappingType(),
                item.getRibbonColor() == null ? RibbonColor.NONE : item.getRibbonColor(),
                item.getMessageText());
    }

    private OrderAddress toOrderAddress(Order order, CheckoutDeliveryData deliveryData) {
        return new OrderAddress(
                order,
                deliveryData.getRecipientName(),
                deliveryData.getPhoneNumber(),
                deliveryData.getPostalCode(),
                deliveryData.getPrefecture(),
                deliveryData.getAddressLine());
    }

    private void decreaseStock(CheckoutOrderSnapshot snapshot) {
        for (CheckoutOrderItemData item : snapshot.getItems()) {
            Product product = item.getProduct();
            Integer currentStock = product.getStock();

            // Confirmation-time stock is not enough; recheck the managed row immediately before decrementing it.
            if (currentStock == null || currentStock < item.getQuantity()) {
                throw new CheckoutConfirmException(
                        "在庫が不足したため注文を確定できません。", CART_REDIRECT);
            }
            product.setStock(currentStock - item.getQuantity());
        }
    }

    private int toOrderAmount(long amount) {
        if (amount < 0 || amount > Integer.MAX_VALUE) {
            throw new CheckoutConfirmException("金額が大きすぎるため注文を確定できません。", CART_REDIRECT);
        }
        return (int) amount;
    }
}
