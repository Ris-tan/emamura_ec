package emamura_ec.dto;

public class OrderDetailItemView {

    private final String productName;
    private final int unitPrice;
    private final int quantity;
    private final long subtotal;
    private final String wrappingDisplayName;
    private final String ribbonDisplayName;
    private final String messageText;

    public OrderDetailItemView(
            String productName,
            int unitPrice,
            int quantity,
            long subtotal,
            String wrappingDisplayName,
            String ribbonDisplayName,
            String messageText) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.wrappingDisplayName = wrappingDisplayName;
        this.ribbonDisplayName = ribbonDisplayName;
        this.messageText = messageText;
    }

    public String getProductName() {
        return productName;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public String getWrappingDisplayName() {
        return wrappingDisplayName;
    }

    public String getRibbonDisplayName() {
        return ribbonDisplayName;
    }

    public String getMessageText() {
        return messageText;
    }
}
