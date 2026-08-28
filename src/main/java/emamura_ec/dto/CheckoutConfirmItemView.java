package emamura_ec.dto;

import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;

public class CheckoutConfirmItemView {

    private final Long productId;
    private final String productName;
    private final String imageUrl;
    private final long unitPrice;
    private final int quantity;
    private final long subtotal;
    private final WrappingType wrappingType;
    private final RibbonColor ribbonColor;
    private final String messageText;

    public CheckoutConfirmItemView(
            Long productId,
            String productName,
            String imageUrl,
            long unitPrice,
            int quantity,
            long subtotal,
            WrappingType wrappingType,
            RibbonColor ribbonColor,
            String messageText) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.wrappingType = wrappingType;
        this.ribbonColor = ribbonColor;
        this.messageText = messageText;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return subtotal;
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
