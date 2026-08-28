package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.dto.CheckoutGiftData;
import emamura_ec.dto.CheckoutGiftItemData;
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
import emamura_ec.exception.CheckoutConfirmException;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.OrderAddressRepository;
import emamura_ec.repository.OrderItemRepository;
import emamura_ec.repository.OrderRepository;
import emamura_ec.repository.ProductRepository;
import emamura_ec.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@SpringBootTest
@Transactional
class OrderPlacementServiceTest {

    @Autowired
    private OrderPlacementService orderPlacementService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderAddressRepository orderAddressRepository;

    @Test
    void 配送注文では注文と明細と配送先を保存し在庫を減らす() {
        TestCheckout checkout = createCheckout(10, 2, DeliveryMethod.SHIPPING, true);

        Long orderId = orderPlacementService.placeOrder(checkout.session(), checkout.user().getEmail());

        Order order = orderRepository.findById(orderId).orElseThrow();
        OrderItem orderItem = orderItemRepository.findAll().get(0);
        OrderAddress orderAddress = orderAddressRepository.findAll().get(0);

        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(LocalDate.now().plusDays(2), order.getRequestedDeliveryDate());
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, order.getPaymentMethod());
        assertEquals(11000, order.getSubtotal());
        assertEquals(500, order.getShippingFee());
        assertEquals(11500, order.getTotalAmount());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(5500, orderItem.getUnitPrice());
        assertEquals(WrappingType.GIFT_WRAP, orderItem.getWrappingType());
        assertEquals(RibbonColor.RED, orderItem.getRibbonColor());
        assertEquals("お祝い", orderItem.getMessageText());
        assertEquals("金沢市本町1-1-1", orderAddress.getAddressLine());
        assertEquals(8, productRepository.findById(checkout.product().getProductId()).orElseThrow().getStock());
    }

    @Test
    void 店頭受取では配送先を保存せず送料をゼロにする() {
        TestCheckout checkout = createCheckout(4, 1, DeliveryMethod.STORE_PICKUP, false);

        Long orderId = orderPlacementService.placeOrder(checkout.session(), checkout.user().getEmail());

        Order order = orderRepository.findById(orderId).orElseThrow();
        OrderItem orderItem = orderItemRepository.findAll().get(0);

        assertEquals(0, order.getShippingFee());
        assertEquals(0, orderAddressRepository.count());
        assertEquals(WrappingType.NONE, orderItem.getWrappingType());
        assertEquals(RibbonColor.NONE, orderItem.getRibbonColor());
        assertNull(orderItem.getMessageText());
    }

    @Test
    void 在庫不足の場合は注文と在庫を変更しない() {
        TestCheckout checkout = createCheckout(1, 2, DeliveryMethod.SHIPPING, false);

        assertThrows(
                CheckoutConfirmException.class,
                () -> orderPlacementService.placeOrder(checkout.session(), checkout.user().getEmail()));

        assertEquals(0, orderRepository.count());
        assertEquals(0, orderItemRepository.count());
        assertEquals(0, orderAddressRepository.count());
        assertEquals(1, productRepository.findById(checkout.product().getProductId()).orElseThrow().getStock());
    }

    @Test
    void 注文成功後は購入用Sessionだけを削除する() {
        TestCheckout checkout = createCheckout(3, 1, DeliveryMethod.STORE_PICKUP, false);
        checkout.session().setAttribute("SPRING_SECURITY_CONTEXT", "login-state");

        orderPlacementService.clearCheckoutSession(checkout.session());

        assertNull(checkout.session().getAttribute("cartItems"));
        assertNull(checkout.session().getAttribute("checkoutGiftEnabled"));
        assertNull(checkout.session().getAttribute("checkoutGift"));
        assertNull(checkout.session().getAttribute("checkoutDeliveryData"));
        assertEquals("login-state", checkout.session().getAttribute("SPRING_SECURITY_CONTEXT"));
    }

    private TestCheckout createCheckout(
            int stock,
            int quantity,
            DeliveryMethod deliveryMethod,
            boolean giftEnabled) {
        String unique = UUID.randomUUID().toString();
        Category category = categoryRepository.save(new Category("花束-" + unique));
        User user = userRepository.save(new User(
                "購入者",
                unique + "@example.com",
                "{bcrypt}test-password",
                "09000000000",
                null));
        Product product = productRepository.save(new Product(
                "テスト花束",
                5500,
                stock,
                category,
                "テスト商品",
                "/images/test.jpg",
                true));

        if (deliveryMethod == DeliveryMethod.SHIPPING) {
            jdbcTemplate.update(
                    "INSERT INTO delivery_areas (prefecture, shipping_fee, lead_days, available) VALUES (?, ?, ?, ?)",
                    "石川県",
                    500,
                    2,
                    true);
        }

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cartItems", Map.of(product.getProductId(), quantity));
        session.setAttribute("checkoutGiftEnabled", giftEnabled);
        session.setAttribute(
                "checkoutGift",
                new CheckoutGiftData(
                        List.of(new CheckoutGiftItemData(
                                product.getProductId(),
                                quantity,
                                giftEnabled ? WrappingType.GIFT_WRAP : WrappingType.NONE,
                                giftEnabled ? RibbonColor.RED : RibbonColor.NONE,
                                giftEnabled ? "お祝い" : null)),
                        giftEnabled ? 0 : 0));
        session.setAttribute(
                "checkoutDeliveryData",
                deliveryMethod == DeliveryMethod.STORE_PICKUP
                        ? new CheckoutDeliveryData(
                                deliveryMethod,
                                "受取人",
                                "09011112222",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null)
                        : new CheckoutDeliveryData(
                                deliveryMethod,
                                "受取人",
                                "09011112222",
                                "9200001",
                                "石川県",
                                "金沢市",
                                "本町1-1-1",
                                500,
                                2,
                                LocalDate.now().plusDays(2)));

        return new TestCheckout(session, user, product);
    }

    private record TestCheckout(HttpSession session, User user, Product product) {
    }
}
