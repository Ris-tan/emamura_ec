package emamura_ec.dto;

public class OrderHistoryItemView {

    private final Long orderId;
    private final String orderDateDisplay;
    private final String orderStatusDisplayName;
    private final int totalAmount;
    private final String deliveryMethodDisplayName;

    public OrderHistoryItemView(
            Long orderId,
            String orderDateDisplay,
            String orderStatusDisplayName,
            int totalAmount,
            String deliveryMethodDisplayName) {
        this.orderId = orderId;
        this.orderDateDisplay = orderDateDisplay;
        this.orderStatusDisplayName = orderStatusDisplayName;
        this.totalAmount = totalAmount;
        this.deliveryMethodDisplayName = deliveryMethodDisplayName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderDateDisplay() {
        return orderDateDisplay;
    }

    public String getOrderStatusDisplayName() {
        return orderStatusDisplayName;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryMethodDisplayName() {
        return deliveryMethodDisplayName;
    }
}
