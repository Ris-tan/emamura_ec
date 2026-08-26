package emamura_ec.dto;

import emamura_ec.entity.Product;
import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;

/**
 * Validated item data shared by the confirmation view and order placement.
 * The Product is the current managed row; it is never stored in the Session.
 */
public class CheckoutOrderItemData {

    private final Product product;
    private final int quantity;
    private final int unitPrice;
    private final WrappingType wrappingType;
    private final RibbonColor ribbonColor;
    private final String messageText;

    public CheckoutOrderItemData(
            Product product,
            int quantity,
            int unitPrice,
            WrappingType wrappingType,
            RibbonColor ribbonColor,
            String messageText) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.wrappingType = wrappingType;
        this.ribbonColor = ribbonColor;
        this.messageText = messageText;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
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

    public long getSubtotal() {
        return (long) unitPrice * quantity;
    }
}
