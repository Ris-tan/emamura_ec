package emamura_ec.dto;

import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;

public class CheckoutGiftItemData {

    private final Long productId;
    private final Integer quantity;
    private final WrappingType wrappingType;
    private final RibbonColor ribbonColor;
    private final String messageText;

    public CheckoutGiftItemData(
            Long productId,
            Integer quantity,
            WrappingType wrappingType,
            RibbonColor ribbonColor,
            String messageText) {
        this.productId = productId;
        this.quantity = quantity;
        this.wrappingType = wrappingType;
        this.ribbonColor = ribbonColor;
        this.messageText = messageText;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public WrappingType getWrappingType() {
        return wrappingType;
    }

    public RibbonColor getRibbonColor() {
        return ribbonColor;
    }

    public String getMessageText() {
        return messageText;
    }
}
