package emamura_ec.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import emamura_ec.dto.CartItemView;
import emamura_ec.dto.CartView;
import emamura_ec.dto.CheckoutGiftData;
import emamura_ec.dto.CheckoutGiftItemData;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import emamura_ec.exception.CheckoutGiftException;
import emamura_ec.form.CheckoutGiftForm;
import emamura_ec.form.GiftItemForm;
import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutGiftService {

    private static final String CHECKOUT_GIFT_SESSION_ATTRIBUTE = "checkoutGift";
    // Paper bags are ordered at the order level, so the current unit price is kept as one meaningful constant.
    private static final int PAPER_BAG_UNIT_PRICE = 50;

    private final CartService cartService;

    public CheckoutGiftService(CartService cartService) {
        this.cartService = cartService;
    }

    public CheckoutGiftForm createForm(HttpSession session) {
        CartView cart = cartService.getCart(session);
        Optional<CheckoutGiftData> savedData = getFromSession(session);
        Map<Long, CheckoutGiftItemData> savedItems = savedData
                .map(CheckoutGiftData::getItems)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(
                        CheckoutGiftItemData::getProductId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new));

        List<GiftItemForm> itemForms = cart.getItems().stream()
                .map(cartItem -> toForm(cartItem, savedItems.get(cartItem.getProduct().getProductId())))
                .toList();
        Integer paperBagCount = savedData.map(CheckoutGiftData::getPaperBagCount).orElse(0);

        return new CheckoutGiftForm(itemForms, paperBagCount);
    }

    public CheckoutGiftData validateAndCreate(HttpSession session, CheckoutGiftForm form) {
        CartView cart = cartService.getCart(session);
        Map<Long, Integer> actualCartItems = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        CartItemView::getQuantity,
                        (first, ignored) -> first,
                        LinkedHashMap::new));

        if (actualCartItems.isEmpty()) {
            throw new CheckoutGiftException("カートに商品がありません。");
        }

        Map<Long, GiftItemForm> submittedItems = toSubmittedItemMap(form);
        if (!submittedItems.keySet().equals(actualCartItems.keySet())) {
            throw new CheckoutGiftException("カート内容が変更されています。ギフト設定をやり直してください。");
        }

        List<CheckoutGiftItemData> giftItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> cartItem : actualCartItems.entrySet()) {
            GiftItemForm submittedItem = submittedItems.get(cartItem.getKey());
            validateGiftItem(submittedItem);

            // 同一商品はカート上で1明細なので、フォーム数量ではなく現在のカート数量を保存する。
            giftItems.add(new CheckoutGiftItemData(
                    cartItem.getKey(),
                    cartItem.getValue(),
                    submittedItem.getWrappingType(),
                    submittedItem.getRibbonColor(),
                    trimToNull(submittedItem.getMessageText())));
        }

        Integer paperBagCount = form.getPaperBagCount();
        if (paperBagCount == null || paperBagCount < 0) {
            throw new CheckoutGiftException("紙袋数量は0枚以上で入力してください。");
        }

        return new CheckoutGiftData(giftItems, paperBagCount);
    }

    public void saveToSession(HttpSession session, CheckoutGiftData data) {
        // 注文確定前は注文Entityへ保存せず、配送Sessionとは独立した一時情報として保持する。
        session.setAttribute(CHECKOUT_GIFT_SESSION_ATTRIBUTE, data);
    }

    public Optional<CheckoutGiftData> getFromSession(HttpSession session) {
        Object savedData = session.getAttribute(CHECKOUT_GIFT_SESSION_ATTRIBUTE);
        if (savedData instanceof CheckoutGiftData data) {
            return Optional.of(data);
        }
        return Optional.empty();
    }

    public int getPaperBagUnitPrice() {
        return PAPER_BAG_UNIT_PRICE;
    }

    public void refreshDisplayData(HttpSession session, CheckoutGiftForm form) {
        Map<Long, CartItemView> cartItems = cartService.getCart(session).getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        Function.identity(),
                        (first, ignored) -> first));

        if (form.getItems() == null) {
            form.setItems(new ArrayList<>());
            return;
        }

        for (GiftItemForm item : form.getItems()) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            CartItemView cartItem = cartItems.get(item.getProductId());
            if (cartItem != null) {
                item.setQuantity(cartItem.getQuantity());
                item.setProductName(cartItem.getProduct().getProductName());
                item.setImageUrl(cartItem.getProduct().getImageUrl());
            }
        }
    }

    private GiftItemForm toForm(CartItemView cartItem, CheckoutGiftItemData savedItem) {
        GiftItemForm form = new GiftItemForm();
        form.setProductId(cartItem.getProduct().getProductId());
        form.setQuantity(cartItem.getQuantity());
        form.setProductName(cartItem.getProduct().getProductName());
        form.setImageUrl(cartItem.getProduct().getImageUrl());
        form.setWrappingType(savedItem == null ? WrappingType.NONE : savedItem.getWrappingType());
        form.setRibbonColor(savedItem == null ? RibbonColor.NONE : savedItem.getRibbonColor());
        form.setMessageText(savedItem == null ? null : savedItem.getMessageText());
        return form;
    }

    private Map<Long, GiftItemForm> toSubmittedItemMap(CheckoutGiftForm form) {
        if (form.getItems() == null) {
            throw new CheckoutGiftException("ギフト設定の商品情報がありません。");
        }

        Map<Long, GiftItemForm> submittedItems = new LinkedHashMap<>();
        for (GiftItemForm item : form.getItems()) {
            if (item == null || item.getProductId() == null) {
                throw new CheckoutGiftException("ギフト設定の商品情報が不正です。");
            }
            if (submittedItems.put(item.getProductId(), item) != null) {
                throw new CheckoutGiftException("同じ商品が重複して送信されています。");
            }
        }
        return submittedItems;
    }

    private void validateGiftItem(GiftItemForm item) {
        if (item.getWrappingType() == null || item.getRibbonColor() == null) {
            throw new CheckoutGiftException("ラッピングとリボンカラーを選択してください。");
        }

        String message = item.getMessageText();
        if (message != null && message.codePointCount(0, message.length()) > 30) {
            throw new CheckoutGiftException("メッセージカードは30文字以内で入力してください。");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
