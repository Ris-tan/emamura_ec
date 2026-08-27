package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.AdminOrderDetailView;
import emamura_ec.dto.AdminOrderListItemView;
import emamura_ec.entity.Category;
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
import emamura_ec.exception.AdminOrderException;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;
import emamura_ec.repository.ProductRepository;
import emamura_ec.repository.UserRepository;

@SpringBootTest
@Transactional
class AdminOrderServiceTest {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderAddressRepository orderAddressRepository;

    @Test
    void findAllOrders_returnsAllUsersOrdersInDescendingOrderDate() {
        User olderUser = createUser("older");
        User newerUser = createUser("newer");
        saveOrder(olderUser, LocalDateTime.of(2026, 8, 25, 10, 0), DeliveryMethod.STORE_PICKUP);
        saveOrder(newerUser, LocalDateTime.of(2026, 8, 27, 10, 0), DeliveryMethod.STORE_PICKUP);

        var orders = adminOrderService.findAllOrders();

        assertEquals(2, orders.size());
        AdminOrderListItemView newest = orders.get(0);
        assertEquals(newerUser.getName(), newest.getUserName());
        assertEquals(newerUser.getEmail(), newest.getUserEmail());
        assertEquals(OrderStatus.PENDING.getDisplayName(), newest.getOrderStatusDisplayName());
    }

    @Test
    void findOrderDetail_integratesOrderItemAddressAndHistoricalAmounts() {
        User user = createUser("detail");
        Product product = createProduct();
        Order order = new Order(
                user,
                LocalDateTime.of(2026, 8, 27, 10, 30),
                OrderStatus.PENDING,
                2468,
                600,
                2,
                70,
                3208,
                DeliveryMethod.SHIPPING,
                null,
                PaymentMethod.CASH_ON_DELIVERY);
        orderRepository.saveAndFlush(order);
        orderItemRepository.save(new OrderItem(
                order,
                product,
                2,
                1234,
                WrappingType.GIFT_WRAP,
                RibbonColor.RED,
                "おめでとう"));
        orderAddressRepository.save(new OrderAddress(
                order,
                "受取人",
                "09011112222",
                "9200001",
                "石川県",
                "金沢市○○町1-1-1"));

        AdminOrderDetailView detail = adminOrderService.findOrderDetail(order.getOrderId());

        assertEquals(user.getName(), detail.getUserName());
        assertEquals(user.getEmail(), detail.getUserEmail());
        assertEquals(1, detail.getItems().size());
        assertEquals(1234, detail.getItems().get(0).getUnitPrice());
        assertEquals(2468, detail.getItems().get(0).getSubtotal());
        assertEquals(WrappingType.GIFT_WRAP.getDisplayName(), detail.getItems().get(0).getWrappingDisplayName());
        assertEquals(RibbonColor.RED.getDisplayName(), detail.getItems().get(0).getRibbonDisplayName());
        assertEquals("金沢市○○町1-1-1", detail.getAddressLine());
        assertEquals(140, detail.getPaperBagTotal());
        assertEquals(3208, detail.getTotalAmount());
    }

    @Test
    void findOrderDetail_forStorePickupDoesNotRequireAddress() {
        User user = createUser("pickup");
        Order order = saveOrder(user, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);

        AdminOrderDetailView detail = adminOrderService.findOrderDetail(order.getOrderId());

        assertFalse(detail.isDeliveryAddressVisible());
        assertNull(detail.getAddressLine());
        assertEquals(DeliveryMethod.STORE_PICKUP.getDisplayName(), detail.getDeliveryMethodDisplayName());
    }

    @Test
    void updateStatus_changesManagedOrderStatus() {
        User user = createUser("status");
        Order order = saveOrder(user, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);

        adminOrderService.updateStatus(order.getOrderId(), "SHIPPED");

        assertEquals(OrderStatus.SHIPPED, orderRepository.findById(order.getOrderId()).orElseThrow().getOrderStatus());
    }

    @Test
    void updateStatus_rejectsUnknownStatus() {
        User user = createUser("invalid-status");
        Order order = saveOrder(user, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);

        assertThrows(
                AdminOrderException.class,
                () -> adminOrderService.updateStatus(order.getOrderId(), "NOT_A_STATUS"));
        assertEquals(OrderStatus.PENDING, orderRepository.findById(order.getOrderId()).orElseThrow().getOrderStatus());
    }

    @Test
    void findOrderDetail_throwsForMissingOrder() {
        assertThrows(
                AdminOrderException.class,
                () -> adminOrderService.findOrderDetail(999999L));
    }

    private User createUser(String suffix) {
        String unique = suffix + "-" + UUID.randomUUID();
        return userRepository.save(new User(
                "テスト注文者 " + suffix,
                unique + "@example.com",
                "{bcrypt}test-password",
                "09000000000",
                null));
    }

    private Product createProduct() {
        Category category = categoryRepository.save(new Category("テストカテゴリ-" + UUID.randomUUID()));
        return productRepository.save(new Product(
                "テスト商品",
                9999,
                10,
                category,
                "テスト説明",
                "/images/test.jpg",
                true));
    }

    private Order saveOrder(User user, LocalDateTime orderDate, DeliveryMethod deliveryMethod) {
        return orderRepository.saveAndFlush(new Order(
                user,
                orderDate,
                OrderStatus.PENDING,
                1000,
                deliveryMethod == DeliveryMethod.STORE_PICKUP ? 0 : 500,
                0,
                50,
                deliveryMethod == DeliveryMethod.STORE_PICKUP ? 1000 : 1500,
                deliveryMethod,
                null,
                PaymentMethod.CASH_ON_DELIVERY));
    }
}
