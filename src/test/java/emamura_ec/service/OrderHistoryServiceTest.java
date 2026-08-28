package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.OrderDetailView;
import emamura_ec.dto.OrderHistoryItemView;
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
import emamura_ec.exception.OrderHistoryException;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;
import emamura_ec.repository.ProductRepository;
import emamura_ec.repository.UserRepository;

@SpringBootTest
@Transactional
class OrderHistoryServiceTest {

    @Autowired
    private OrderHistoryService orderHistoryService;

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
    void ユーザー自身の注文だけを新しい順で取得する() {
        User userA = createUser("a@example.com");
        User userB = createUser("b@example.com");
        Order aOld = saveOrder(userA, LocalDateTime.of(2026, 8, 25, 10, 0), DeliveryMethod.STORE_PICKUP);
        Order aNew = saveOrder(userA, LocalDateTime.of(2026, 8, 26, 10, 0), DeliveryMethod.STORE_PICKUP);
        saveOrder(userB, LocalDateTime.of(2026, 8, 27, 10, 0), DeliveryMethod.STORE_PICKUP);

        var history = orderHistoryService.findOrders(userA.getEmail());

        assertEquals(2, history.size());
        assertEquals(aNew.getOrderId(), history.get(0).getOrderId());
        assertEquals(aOld.getOrderId(), history.get(1).getOrderId());
        assertEquals("注文受付済み", history.get(0).getOrderStatusDisplayName());
        assertEquals("店頭受取", history.get(0).getDeliveryMethodDisplayName());
    }

    @Test
    void 他ユーザーの注文詳細は取得できない() {
        User userA = createUser("a-detail@example.com");
        User userB = createUser("b-detail@example.com");
        Order orderB = saveOrder(userB, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);

        assertThrows(
                OrderHistoryException.class,
                () -> orderHistoryService.findOrderDetail(userA.getEmail(), orderB.getOrderId()));
    }

    @Test
    void 配送注文はOrderAddressと保存済み金額を表示する() {
        User user = createUser("shipping@example.com");
        Product product = createProduct();
        Order order = new Order(
                user,
                LocalDateTime.of(2026, 8, 26, 15, 30),
                OrderStatus.PENDING,
                2468,
                600,
                2,
                70,
                3208,
                DeliveryMethod.SHIPPING,
                LocalDate.of(2026, 8, 30),
                PaymentMethod.CASH_ON_DELIVERY);
        orderRepository.saveAndFlush(order);
        orderItemRepository.save(new OrderItem(
                order,
                product,
                2,
                1234,
                WrappingType.GIFT_WRAP,
                RibbonColor.RED,
                "お祝い"));
        orderAddressRepository.save(new OrderAddress(
                order,
                "受取人",
                "09011112222",
                "9200001",
                "石川県",
                "金沢市本町1-1-1"));

        OrderDetailView detail = orderHistoryService.findOrderDetail(user.getEmail(), order.getOrderId());

        assertTrue(detail.isDeliveryAddressVisible());
        assertTrue(detail.isShippingFeeVisible());
        assertEquals("受取人", detail.getRecipientName());
        assertEquals("金沢市本町1-1-1", detail.getAddressLine());
        assertEquals("2026/08/30", detail.getRequestedDeliveryDateDisplay());
        assertEquals(1234, detail.getItems().get(0).getUnitPrice());
        assertEquals(2468, detail.getItems().get(0).getSubtotal());
        assertEquals("ギフトラッピング", detail.getItems().get(0).getWrappingDisplayName());
        assertEquals("赤", detail.getItems().get(0).getRibbonDisplayName());
        assertEquals(140, detail.getPaperBagTotal());
        assertEquals(3208, detail.getTotalAmount());
    }

    @Test
    void 店頭受取は配送先を表示しない() {
        User user = createUser("pickup@example.com");
        Order order = saveOrder(user, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);

        OrderDetailView detail = orderHistoryService.findOrderDetail(user.getEmail(), order.getOrderId());

        assertFalse(detail.isDeliveryAddressVisible());
        assertFalse(detail.isShippingFeeVisible());
        assertNull(detail.getRecipientName());
        assertEquals("店頭受取", detail.getDeliveryMethodDisplayName());
    }

    @Test
    void 注文履歴がない場合は空の一覧を返す() {
        User user = createUser("empty@example.com");

        assertTrue(orderHistoryService.findOrders(user.getEmail()).isEmpty());
    }

    @Test
    void 完了ステータスの表示は受取方法で変わる() {
        User shippingUser = createUser("completed-shipping@example.com");
        Order shippingOrder = saveOrder(shippingUser, LocalDateTime.now(), DeliveryMethod.SHIPPING);
        shippingOrder.setOrderStatus(OrderStatus.COMPLETED);

        User pickupUser = createUser("completed-pickup@example.com");
        Order pickupOrder = saveOrder(pickupUser, LocalDateTime.now(), DeliveryMethod.STORE_PICKUP);
        pickupOrder.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.flush();

        assertEquals(
                "配達完了",
                orderHistoryService.findOrders(shippingUser.getEmail()).get(0).getOrderStatusDisplayName());
        assertEquals(
                "受け渡し完了",
                orderHistoryService.findOrders(pickupUser.getEmail()).get(0).getOrderStatusDisplayName());
    }

    private User createUser(String email) {
        return userRepository.save(new User(
                "注文履歴テストユーザー",
                email,
                "{bcrypt}test-password",
                "09000000000",
                null));
    }

    private Product createProduct() {
        Category category = categoryRepository.save(new Category("履歴テストカテゴリ" + System.nanoTime()));
        return productRepository.save(new Product(
                "履歴テスト商品",
                9999,
                10,
                category,
                "説明",
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
