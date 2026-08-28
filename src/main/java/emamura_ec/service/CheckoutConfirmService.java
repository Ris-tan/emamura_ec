package emamura_ec.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import emamura_ec.dto.CheckoutConfirmItemView;
import emamura_ec.dto.CheckoutConfirmView;
import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.dto.CheckoutGiftData;
import emamura_ec.dto.CheckoutGiftItemData;
import emamura_ec.dto.CheckoutOrderItemData;
import emamura_ec.dto.CheckoutOrderSnapshot;
import emamura_ec.entity.DeliveryMethod;
import emamura_ec.entity.Product;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import emamura_ec.exception.CheckoutConfirmException;
import emamura_ec.exception.CheckoutDeliveryException;
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
        CheckoutOrderSnapshot snapshot = createSnapshot(session);
        CheckoutDeliveryData deliveryData = snapshot.getDeliveryData();

        List<CheckoutConfirmItemView> itemViews = snapshot.getItems().stream()
                .map(item -> new CheckoutConfirmItemView(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getProduct().getImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getWrappingType(),
                        item.getRibbonColor(),
                        item.getMessageText()))
                .toList();

        // The view DTO combines Session choices and current DB values without saving an order before confirmation.
        return new CheckoutConfirmView(
                itemViews,
                snapshot.isGiftEnabled(),
                toDeliveryMethodDisplayName(deliveryData.getDeliveryMethod()),
                deliveryData.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP,
                deliveryData.getDeliveryMethod() == DeliveryMethod.SHIPPING,
                deliveryData.getRecipientName(),
                deliveryData.getPhoneNumber(),
                deliveryData.getPostalCode(),
                deliveryData.getPrefecture(),
                deliveryData.getCityAddress(),
                deliveryData.getAddressDetail(),
                snapshot.getEarliestDeliveryDate() != null,
                snapshot.getEarliestDeliveryDate() == null
                        ? null
                        : checkoutDeliveryService.formatDeliveryDate(snapshot.getEarliestDeliveryDate()),
                checkoutDeliveryService.formatDeliveryDate(deliveryData.getRequestedDeliveryDate()),
                snapshot.getProductSubtotal(),
                snapshot.getShippingFee(),
                snapshot.getPaperBagCount(),
                snapshot.getPaperBagUnitPrice(),
                snapshot.getPaperBagTotal(),
                snapshot.getTotal());
    }

    /**
     * Builds one validated snapshot for both confirmation display and order placement.
     * The placement POST calls this again, so an old confirmation page cannot determine the final price or stock.
     */
    public CheckoutOrderSnapshot createSnapshot(HttpSession session) {
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
        validateDeliveryData(deliveryData);
        LocalDate earliestDeliveryDate;
        try {
            // The delivery master is checked again because confirmation and placement can happen on different days.
            earliestDeliveryDate = checkoutDeliveryService.validateStoredDeliveryData(deliveryData);
        } catch (CheckoutDeliveryException exception) {
            throw new CheckoutConfirmException(exception.getMessage(), DELIVERY_REDIRECT);
        }

        boolean giftEnabled = checkoutGiftService.isGiftEnabled(session);
        Map<Long, CheckoutGiftItemData> giftItems = giftEnabled
                ? getGiftItemsForCart(session, cartItems)
                : Map.of();

        List<CheckoutOrderItemData> items = cartItems.entrySet().stream()
                .map(entry -> createOrderItemData(entry.getKey(), entry.getValue(), giftEnabled, giftItems))
                .toList();

        long productSubtotal = items.stream()
                .mapToLong(CheckoutOrderItemData::getSubtotal)
                .reduce(0L, this::addAmount);
        long shippingFee = resolveShippingFee(deliveryData);
        int paperBagCount = giftEnabled ? getPaperBagCount(session) : 0;
        int paperBagUnitPrice = checkoutGiftService.getPaperBagUnitPrice();
        long paperBagTotal = giftEnabled
                ? multiplyAmount(paperBagCount, paperBagUnitPrice)
                : 0L;
        long total = addAmount(addAmount(productSubtotal, shippingFee), paperBagTotal);

        // These columns are INTEGER in the existing DDL, so reject amounts that cannot be persisted safely.
        ensureOrderAmount(productSubtotal);
        ensureOrderAmount(shippingFee);
        ensureOrderAmount(paperBagTotal);
        ensureOrderAmount(total);

        return new CheckoutOrderSnapshot(
                items,
                giftEnabled,
                deliveryData,
                productSubtotal,
                shippingFee,
                paperBagCount,
                paperBagUnitPrice,
                paperBagTotal,
                total,
                earliestDeliveryDate);
    }

    private CheckoutOrderItemData createOrderItemData(
            Long productId,
            Integer quantity,
            boolean giftEnabled,
            Map<Long, CheckoutGiftItemData> giftItems) {
        if (productId == null || quantity == null || quantity < 1) {
            throw new CheckoutConfirmException("カートの商品数量が不正です。", CART_REDIRECT);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CheckoutConfirmException(
                        "カートの商品が現在販売されていません。", CART_REDIRECT));

        // Price, active state, and stock are checked again at POST time because the confirmation page can be stale.
        if (!Boolean.TRUE.equals(product.getIsActive())
                || product.getPrice() == null
                || product.getPrice() < 0
                || product.getStock() == null
                || product.getStock() < 0
                || quantity > product.getStock()) {
            throw new CheckoutConfirmException(
                    "商品の販売状態、価格、または在庫状況が変更されています。カートを確認してください。", CART_REDIRECT);
        }

        CheckoutGiftItemData giftItem = giftEnabled ? giftItems.get(productId) : null;
        WrappingType wrappingType = giftItem == null ? WrappingType.NONE : giftItem.getWrappingType();
        RibbonColor ribbonColor = giftItem == null ? RibbonColor.NONE : giftItem.getRibbonColor();
        String messageText = giftItem == null ? null : giftItem.getMessageText();

        return new CheckoutOrderItemData(
                product,
                quantity,
                product.getPrice(),
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

    private void validateDeliveryData(CheckoutDeliveryData deliveryData) {
        if (deliveryData.getDeliveryMethod() == null
                || !StringUtils.hasText(deliveryData.getRecipientName())
                || !StringUtils.hasText(deliveryData.getPhoneNumber())) {
            throw new CheckoutConfirmException("受取方法・受取人情報を確認してください。", DELIVERY_REDIRECT);
        }

        if (deliveryData.getDeliveryMethod() != DeliveryMethod.STORE_PICKUP
                && (!StringUtils.hasText(deliveryData.getPostalCode())
                || !StringUtils.hasText(deliveryData.getPrefecture())
                || !StringUtils.hasText(deliveryData.getAddressLine())
                || deliveryData.getAddressLine().length() > 255)) {
            // UserAddress stores one address_line column, so a saved address may not have separate city/detail parts.
            throw new CheckoutConfirmException("配送先情報を確認してください。", DELIVERY_REDIRECT);
        }
    }

    private long resolveShippingFee(CheckoutDeliveryData deliveryData) {
        if (deliveryData.getDeliveryMethod() == DeliveryMethod.STORE_PICKUP) {
            // Store pickup has no delivery charge and does not require an order address.
            return 0L;
        }
        if (deliveryData.getShippingFee() == null || deliveryData.getShippingFee() < 0) {
            throw new CheckoutConfirmException("送料情報が見つかりません。", DELIVERY_REDIRECT);
        }
        return deliveryData.getShippingFee();
    }

    private String toDeliveryMethodDisplayName(DeliveryMethod deliveryMethod) {
        return deliveryMethod == DeliveryMethod.STORE_PICKUP ? "店頭受取" : "お届け";
    }

    private void ensureOrderAmount(long amount) {
        if (amount < 0 || amount > Integer.MAX_VALUE) {
            throw new CheckoutConfirmException("金額が大きすぎるため注文を確定できません。", CART_REDIRECT);
        }
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
