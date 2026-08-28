package emamura_ec.dto;

public class AdminOrderListItemView {

    private final Long orderId;
    private final String orderDateDisplay;
    private final String userName;
    private final String userEmail;
    private final String deliveryMethodDisplayName;
    private final String orderStatusDisplayName;
    private final int totalAmount;

    public AdminOrderListItemView(
            Long orderId,
            String orderDateDisplay,
            String userName,
            String userEmail,
            String deliveryMethodDisplayName,
            String orderStatusDisplayName,
            int totalAmount) {
        this.orderId = orderId;
        this.orderDateDisplay = orderDateDisplay;
        this.userName = userName;
        this.userEmail = userEmail;
        this.deliveryMethodDisplayName = deliveryMethodDisplayName;
        this.orderStatusDisplayName = orderStatusDisplayName;
        this.totalAmount = totalAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderDateDisplay() {
        return orderDateDisplay;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getDeliveryMethodDisplayName() {
        return deliveryMethodDisplayName;
    }

    public String getOrderStatusDisplayName() {
        return orderStatusDisplayName;
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
