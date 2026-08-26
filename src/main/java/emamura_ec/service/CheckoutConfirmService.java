package emamura_ec.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import emamura_ec.dto.CheckoutConfirmItemView;
import emamura_ec.dto.CheckoutConfirmView;
import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.dto.CheckoutGiftData;
import emamura_ec.dto.CheckoutGiftItemData;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.Product;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import emamura_ec.exception.CheckoutConfirmException;
import emamura_ec.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutConfirmService {

    private static final String CART_REDIRECT = "/cart";
    private static final String DELIVERY_REDIRECT = "/checkout/delivery";
    private static final String GIFT_REDIRECT = "/checkout/gift";

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CheckoutGiftService checkoutGiftService;
    private final CheckoutDeliveryService checkoutDeliveryService;

    public CheckoutConfirmService(
            CartService cartService,
            ProductRepository productRepository,
            CheckoutGiftService checkoutGiftService,
            CheckoutDeliveryService checkoutDeliveryService) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.checkoutGiftService = checkoutGiftService;
        this.checkoutDeliveryService = checkoutDeliveryService;
    }

    public CheckoutConfirmView createView(HttpSession session) {
        if (!checkoutGiftService.isCheckoutStarted(session)) {
            throw new CheckoutConfirmException("購入手続きが開始されていません。", CART_REDIRECT);
        }

        Map<Long, Integer> cartItems = cartService.getCartItemsSnapshot(session);
        if (cartItems.isEmpty()) {
            throw new CheckoutConfirmException("カートに商品がありません。", CART_REDIRECT);
        }

        CheckoutDeliveryData deliveryData = checkoutDeliveryService.getFromSession(session)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "受取方法・配送先情報を入力してください。", DELIVERY_REDIRECT));

        boolean giftEnabled = checkoutGiftService.isGiftEnabled(session);
        Map<Long, CheckoutGiftItemData> giftItems = giftEnabled
                ? getGiftItemsForCart(session, cartItems)
                : Map.of();

        List<CheckoutConfirmItemView> itemViews = cartItems.entrySet().stream()
                .map(entry -> createItemView(entry.getKey(), entry.getValue(), giftEnabled, giftItems))
                .toList();

        long productSubtotal = itemViews.stream()
                .mapToLong(CheckoutConfirmItemView::getSubtotal)
                .reduce(0L, this::addAmount);

        int shippingFee = resolveShippingFee(deliveryData);
        int paperBagCount = giftEnabled
                ? getPaperBagCount(session)
                : 0;
        long paperBagUnitPrice = checkoutGiftService.getPaperBagUnitPrice();
        long paperBagTotal = giftEnabled
                ? multiplyAmount(paperBagCount, paperBagUnitPrice)
                : 0L;
        long total = addAmount(addAmount(productSubtotal, shippingFee), paperBagTotal);

        // This view combines Session data and current DB data without saving an order before confirmation.
        return new CheckoutConfirmView(
                itemViews,
                giftEnabled,
                toDeliveryMethodDisplayName(deliveryData.getDeliveryMethod()),
                deliveryData.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP,
                deliveryData.getRecipientName(),
                deliveryData.getPhoneNumber(),
                deliveryData.getPostalCode(),
                deliveryData.getPrefecture(),
                deliveryData.getCityAddress(),
                deliveryData.getAddressDetail(),
                productSubtotal,
                shippingFee,
                paperBagCount,
                paperBagUnitPrice,
                paperBagTotal,
                total);
    }

    private CheckoutConfirmItemView createItemView(
            Long productId,
            Integer quantity,
            boolean giftEnabled,
            Map<Long, CheckoutGiftItemData> giftItems) {
        if (quantity == null || quantity < 1) {
            throw new CheckoutConfirmException("カートの商品数量が不正です。", CART_REDIRECT);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "カートの商品が現在販売されていません。", CART_REDIRECT));

        // Prices and stock can change after the cart was created, so confirmation rechecks the current Product row.
        if (product.getPrice() == null || product.getStock() == null || quantity > product.getStock()) {
            throw new CheckoutConfirmException(
                    "商品の価格または在庫状況が変更されています。カートを確認してください。", CART_REDIRECT);
        }

        long subtotal = multiplyAmount(quantity, product.getPrice());
        CheckoutGiftItemData giftItem = giftEnabled
                ? giftItems.get(productId)
                : null;
        WrappingType wrappingType = giftItem == null ? WrappingType.NONE : giftItem.getWrappingType();
        RibbonColor ribbonColor = giftItem == null ? RibbonColor.NONE : giftItem.getRibbonColor();
        String messageText = giftItem == null ? null : giftItem.getMessageText();

        return new CheckoutConfirmItemView(
                product.getProductId(),
                product.getProductName(),
                product.getImageUrl(),
                product.getPrice(),
                quantity,
                subtotal,
                wrappingType,
                ribbonColor,
                messageText);
    }

    private Map<Long, CheckoutGiftItemData> getGiftItemsForCart(
            HttpSession session,
            Map<Long, Integer> cartItems) {
        CheckoutGiftData giftData = checkoutGiftService.getFromSession(session)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "ギフト設定が見つかりません。", GIFT_REDIRECT));

        Map<Long, CheckoutGiftItemData> giftItems = new LinkedHashMap<>();
        if (giftData.getItems() == null) {
            throw new CheckoutConfirmException("ギフト設定の内容が見つかりません。", GIFT_REDIRECT);
        }
        for (CheckoutGiftItemData item : giftData.getItems()) {
            if (item == null || item.getProductId() == null
                    || giftItems.put(item.getProductId(), item) != null) {
                throw new CheckoutConfirmException("ギフト設定の商品情報が不正です。", GIFT_REDIRECT);
            }
        }

        if (!giftItems.keySet().equals(cartItems.keySet())) {
            throw new CheckoutConfirmException(
                    "カート内容とギフト設定が一致しません。ギフト設定をやり直してください。", GIFT_REDIRECT);
        }

        for (Map.Entry<Long, CheckoutGiftItemData> entry : giftItems.entrySet()) {
            CheckoutGiftItemData item = entry.getValue();
            if (!Integer.valueOf(cartItems.get(entry.getKey())).equals(item.getQuantity())
                    || item.getWrappingType() == null
                    || item.getRibbonColor() == null
                    || (item.getMessageText() != null
                    && item.getMessageText().codePointCount(0, item.getMessageText().length()) > 30)) {
                throw new CheckoutConfirmException(
                        "ギフト設定が現在のカートと一致しません。", GIFT_REDIRECT);
            }
        }
        return giftItems;
    }

    private int getPaperBagCount(HttpSession session) {
        CheckoutGiftData giftData = checkoutGiftService.getFromSession(session)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "ギフト設定が見つかりません。", GIFT_REDIRECT));
        Integer paperBagCount = giftData.getPaperBagCount();
        if (paperBagCount == null || paperBagCount < 0) {
            throw new CheckoutConfirmException("紙袋数量が不正です。", GIFT_REDIRECT);
        }
        return paperBagCount;
    }

    private int resolveShippingFee(CheckoutDeliveryData deliveryData) {
        if (deliveryData.getDeliveryMethod() == null) {
            throw new CheckoutConfirmException("受取方法が見つかりません。", DELIVERY_REDIRECT);
        }
        if (deliveryData.getDeliveryMethod() == DeliveryMethod.STORE_PICKUP) {
            // Store pickup does not use a delivery area or shipping fee.
            return 0;
        }
        if (deliveryData.getShippingFee() == null || deliveryData.getShippingFee() < 0) {
            throw new CheckoutConfirmException("送料情報が見つかりません。", DELIVERY_REDIRECT);
        }
        return deliveryData.getShippingFee();
    }

    private String toDeliveryMethodDisplayName(DeliveryMethod deliveryMethod) {
        if (deliveryMethod == null) {
            throw new CheckoutConfirmException("受取方法が見つかりません。", DELIVERY_REDIRECT);
        }
        return deliveryMethod == DeliveryMethod.STORE_PICKUP ? "店頭受取" : "お届け";
    }

    private long multiplyAmount(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new CheckoutConfirmException("金額が大きすぎるため確認できません。", CART_REDIRECT);
        }
    }

    private long addAmount(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new CheckoutConfirmException("金額が大きすぎるため確認できません。", CART_REDIRECT);
        }
    }
}
