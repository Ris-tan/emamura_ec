package emamura_ec.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.OrderDetailItemView;
import emamura_ec.dto.OrderDetailView;
import emamura_ec.dto.OrderHistoryItemView;
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
import emamura_ec.exception.OrderHistoryException;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;
import emamura_ec.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class OrderHistoryService {

    private static final DateTimeFormatter ORDER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;

    public OrderHistoryService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderAddressRepository orderAddressRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
    }

    public List<OrderHistoryItemView> findOrders(String loginEmail) {
        User user = findUser(loginEmail);
        return orderRepository.findByUserOrderByOrderDateDesc(user).stream()
                .map(this::toHistoryItemView)
                .toList();
    }

    public OrderDetailView findOrderDetail(String loginEmail, Long orderId) {
        User user = findUser(loginEmail);

        // Filtering by both ID and owner prevents a guessed URL from exposing another user's order.
        Order order = orderRepository.findByOrderIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderHistoryException("注文情報が見つかりません。"));

        List<OrderItem> orderItems = orderItemRepository
                .findByOrder_OrderIdOrderByOrderItemIdAsc(order.getOrderId());
        List<OrderDetailItemView> itemViews = orderItems.stream()
                .map(this::toDetailItemView)
                .toList();

        boolean deliveryAddressVisible = order.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP;
        OrderAddress address = deliveryAddressVisible
                ? orderAddressRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new OrderHistoryException("注文情報が見つかりません。"))
                : null;

        int paperBagCount = requireAmount(order.getPaperBagCount());
        int paperBagUnitPrice = requireAmount(order.getPaperBagUnitPrice());
        long paperBagTotal = multiplyAmount(paperBagCount, paperBagUnitPrice);

        return new OrderDetailView(
                order.getOrderId(),
                formatOrderDate(order.getOrderDate()),
                displayName(order.getOrderStatus()),
                displayName(order.getDeliveryMethod()),
                displayName(order.getPaymentMethod()),
                itemViews,
                deliveryAddressVisible,
                address == null ? null : address.getRecipientName(),
                address == null ? null : address.getPhoneNumber(),
                address == null ? null : address.getPostalCode(),
                address == null ? null : address.getPrefecture(),
                address == null ? null : address.getAddressLine(),
                requireAmount(order.getSubtotal()),
                requireAmount(order.getShippingFee()),
                paperBagCount,
                paperBagUnitPrice,
                paperBagTotal,
                requireAmount(order.getTotalAmount()));
    }

    private User findUser(String loginEmail) {
        return userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new OrderHistoryException("注文情報が見つかりません。"));
    }

    private OrderHistoryItemView toHistoryItemView(Order order) {
        return new OrderHistoryItemView(
                order.getOrderId(),
                formatOrderDate(order.getOrderDate()),
                displayName(order.getOrderStatus()),
                requireAmount(order.getTotalAmount()),
                displayName(order.getDeliveryMethod()));
    }

    private OrderDetailItemView toDetailItemView(OrderItem orderItem) {
        Integer unitPrice = orderItem.getUnitPrice();
        Integer quantity = orderItem.getQuantity();
        if (unitPrice == null || unitPrice < 0 || quantity == null || quantity < 1) {
            throw new OrderHistoryException("注文情報が見つかりません。");
        }

        // OrderItem.unitPrice is the historical price snapshot; Product.price may have changed since this order.
        Product product = orderItem.getProduct();
        String productName = product == null ? "商品情報なし" : product.getProductName();
        return new OrderDetailItemView(
                productName,
                unitPrice,
                quantity,
                multiplyAmount(unitPrice, quantity),
                displayName(orderItem.getWrappingType()),
                displayName(orderItem.getRibbonColor()),
                orderItem.getMessageText());
    }

    private String formatOrderDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            throw new OrderHistoryException("注文情報が見つかりません。");
        }
        return ORDER_DATE_FORMATTER.format(orderDate);
    }

    private String displayName(Enum<?> value) {
        if (value instanceof OrderStatus orderStatus) {
            return orderStatus.getDisplayName();
        }
        if (value instanceof DeliveryMethod deliveryMethod) {
            return deliveryMethod.getDisplayName();
        }
        if (value instanceof PaymentMethod paymentMethod) {
            return paymentMethod.getDisplayName();
        }
        if (value instanceof WrappingType wrappingType) {
            return wrappingType.getDisplayName();
        }
        if (value instanceof RibbonColor ribbonColor) {
            return ribbonColor.getDisplayName();
        }
        return "不明";
    }

    private int requireAmount(Integer amount) {
        if (amount == null || amount < 0) {
            throw new OrderHistoryException("注文情報が見つかりません。");
        }
        return amount;
    }

    private long multiplyAmount(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new OrderHistoryException("注文金額を表示できません。");
        }
    }
}
