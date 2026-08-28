package emamura_ec.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.AdminOrderDetailItemView;
import emamura_ec.dto.AdminOrderDetailView;
import emamura_ec.dto.AdminOrderListItemView;
import emamura_ec.dto.AdminOrderStatusOption;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.Order;
import emamura_ec.entity.OrderAddress;
import emamura_ec.entity.OrderItem;
import emamura_ec.entity.OrderStatus;
import emamura_ec.entity.PaymentMethod;
import emamura_ec.entity.Product;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import emamura_ec.exception.AdminOrderException;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;

@Service
public class AdminOrderService {

    private static final DateTimeFormatter ORDER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter REQUESTED_DELIVERY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;

    public AdminOrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderAddressRepository orderAddressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminOrderListItemView> findAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::toListItemView)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailView findOrderDetail(Long orderId) {
        Order order = findOrder(orderId);
        List<OrderItem> orderItems = orderItemRepository
                .findByOrder_OrderIdOrderByOrderItemIdAsc(order.getOrderId());
        List<AdminOrderDetailItemView> itemViews = orderItems.stream()
                .map(this::toDetailItemView)
                .toList();

        // The order record is the historical snapshot. Do not recalculate old orders from current product or delivery master data.
        boolean deliveryAddressVisible = order.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP;
        OrderAddress address = deliveryAddressVisible
                ? orderAddressRepository.findByOrder_OrderId(order.getOrderId())
                        .orElseThrow(() -> new AdminOrderException("Order address was not found."))
                : null;

        int paperBagCount = requireAmount(order.getPaperBagCount());
        int paperBagUnitPrice = requireAmount(order.getPaperBagUnitPrice());
        long paperBagTotal = multiplyAmount(paperBagCount, paperBagUnitPrice);
        // This is a historical customer request, not a value to recalculate from today's delivery master.
        boolean requestedDeliveryDateVisible = order.getDeliveryMethod() == DeliveryMethod.SHIPPING;
        boolean shippingFeeVisible = order.getDeliveryMethod() == DeliveryMethod.SHIPPING;

        if (order.getUser() == null) {
            throw new AdminOrderException("Order customer was not found.");
        }

        return new AdminOrderDetailView(
                order.getOrderId(),
                formatOrderDate(order.getOrderDate()),
                displayName(order.getOrderStatus(), order.getDeliveryMethod()),
                order.getOrderStatus(),
                displayName(order.getDeliveryMethod()),
                displayName(order.getPaymentMethod()),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getUser().getPhoneNumber(),
                itemViews,
                deliveryAddressVisible,
                shippingFeeVisible,
                address == null ? null : address.getRecipientName(),
                address == null ? null : address.getPhoneNumber(),
                address == null ? null : address.getPostalCode(),
                address == null ? null : address.getPrefecture(),
                address == null ? null : address.getAddressLine(),
                requestedDeliveryDateVisible,
                requestedDeliveryDateVisible
                        ? formatRequestedDeliveryDate(order.getRequestedDeliveryDate())
                        : null,
                requireAmount(order.getSubtotal()),
                requireAmount(order.getShippingFee()),
                paperBagCount,
                paperBagUnitPrice,
                paperBagTotal,
                requireAmount(order.getTotalAmount()));
    }

    public List<AdminOrderStatusOption> getOrderStatusOptions(Long orderId) {
        Order order = findOrder(orderId);
        return getOrderStatuses(order.getDeliveryMethod()).stream()
                .map(status -> new AdminOrderStatusOption(
                        status,
                        displayName(status, order.getDeliveryMethod())))
                .toList();
    }

    /**
     * Shipping and pickup have different operational milestones, so a status
     * valid for one delivery method must not be offered or accepted for the other.
     */
    public List<OrderStatus> getOrderStatuses(DeliveryMethod deliveryMethod) {
        if (deliveryMethod == DeliveryMethod.STORE_PICKUP) {
            return List.of(
                    OrderStatus.PENDING,
                    OrderStatus.PREPARING,
                    OrderStatus.READY_FOR_PICKUP,
                    OrderStatus.COMPLETED,
                    OrderStatus.CANCELLED);
        }
        if (deliveryMethod == DeliveryMethod.SHIPPING || deliveryMethod == DeliveryMethod.LOCAL_DELIVERY) {
            return List.of(
                    OrderStatus.PENDING,
                    OrderStatus.PREPARING,
                    OrderStatus.SHIPPED,
                    OrderStatus.COMPLETED,
                    OrderStatus.CANCELLED);
        }
        throw new AdminOrderException("Delivery method is invalid.");
    }

    @Transactional
    public void updateStatus(Long orderId, String statusValue) {
        Order order = findOrder(orderId);
        OrderStatus status = parseStatus(statusValue);
        if (!getOrderStatuses(order.getDeliveryMethod()).contains(status)) {
            throw new AdminOrderException("The selected status is not available for this delivery method.");
        }

        // A POST-only update inside a transaction keeps the state change explicit and lets JPA dirty checking persist it on commit.
        order.setOrderStatus(status);
    }

    private Order findOrder(Long orderId) {
        if (orderId == null) {
            throw new AdminOrderException("Order was not found.");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AdminOrderException("Order was not found."));
    }

    private AdminOrderListItemView toListItemView(Order order) {
        if (order.getUser() == null) {
            throw new AdminOrderException("Order customer was not found.");
        }
        return new AdminOrderListItemView(
                order.getOrderId(),
                formatOrderDate(order.getOrderDate()),
                order.getUser().getName(),
                order.getUser().getEmail(),
                displayName(order.getDeliveryMethod()),
                displayName(order.getOrderStatus(), order.getDeliveryMethod()),
                requireAmount(order.getTotalAmount()));
    }

    private AdminOrderDetailItemView toDetailItemView(OrderItem orderItem) {
        Integer unitPrice = orderItem.getUnitPrice();
        Integer quantity = orderItem.getQuantity();
        if (unitPrice == null || unitPrice < 0 || quantity == null || quantity < 1) {
            throw new AdminOrderException("Order item amount or quantity is invalid.");
        }

        // OrderItem.unitPrice is the price agreed at purchase time; Product.price is allowed to change later.
        Product product = orderItem.getProduct();
        String productName = product == null ? "Product information unavailable" : product.getProductName();
        return new AdminOrderDetailItemView(
                productName,
                unitPrice,
                quantity,
                multiplyAmount(unitPrice, quantity),
                displayName(orderItem.getWrappingType()),
                displayName(orderItem.getRibbonColor()),
                orderItem.getMessageText());
    }

    private OrderStatus parseStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            throw new AdminOrderException("Invalid order status.");
        }
        try {
            return OrderStatus.valueOf(statusValue);
        } catch (IllegalArgumentException exception) {
            throw new AdminOrderException("Invalid order status.");
        }
    }

    private String formatOrderDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            throw new AdminOrderException("Order date is invalid.");
        }
        return ORDER_DATE_FORMATTER.format(orderDate);
    }

    private String formatRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        return requestedDeliveryDate == null
                ? "指定なし"
                : REQUESTED_DELIVERY_DATE_FORMATTER.format(requestedDeliveryDate);
    }

    private String displayName(OrderStatus orderStatus, DeliveryMethod deliveryMethod) {
        if ((orderStatus == OrderStatus.COMPLETED || orderStatus == OrderStatus.DELIVERED)
                && deliveryMethod == DeliveryMethod.STORE_PICKUP) {
            return "受け渡し完了";
        }
        if (orderStatus == OrderStatus.COMPLETED || orderStatus == OrderStatus.DELIVERED) {
            return "配達完了";
        }
        return displayName(orderStatus);
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
        return "Unknown";
    }

    private int requireAmount(Integer amount) {
        if (amount == null || amount < 0) {
            throw new AdminOrderException("Order amount is invalid.");
        }
        return amount;
    }

    private long multiplyAmount(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AdminOrderException("Amount cannot be displayed.");
        }
    }
}
