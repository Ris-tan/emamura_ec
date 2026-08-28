package emamura_ec.dto;

import java.util.List;

import emamura_ec.entity.OrderStatus;

public class AdminOrderDetailView {

    private final Long orderId;
    private final String orderDateDisplay;
    private final String orderStatusDisplayName;
    private final OrderStatus orderStatus;
    private final String deliveryMethodDisplayName;
    private final String paymentMethodDisplayName;
    private final String userName;
    private final String userEmail;
    private final String userPhoneNumber;
    private final List<AdminOrderDetailItemView> items;
    private final boolean deliveryAddressVisible;
    private final boolean shippingFeeVisible;
    private final String recipientName;
    private final String recipientPhoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String addressLine;
    private final boolean requestedDeliveryDateVisible;
    private final String requestedDeliveryDateDisplay;
    private final int productSubtotal;
    private final int shippingFee;
    private final int paperBagCount;
    private final int paperBagUnitPrice;
    private final long paperBagTotal;
    private final int totalAmount;

    public AdminOrderDetailView(
            Long orderId,
            String orderDateDisplay,
            String orderStatusDisplayName,
            OrderStatus orderStatus,
            String deliveryMethodDisplayName,
            String paymentMethodDisplayName,
            String userName,
            String userEmail,
            String userPhoneNumber,
            List<AdminOrderDetailItemView> items,
            boolean deliveryAddressVisible,
            boolean shippingFeeVisible,
            String recipientName,
            String recipientPhoneNumber,
            String postalCode,
            String prefecture,
            String addressLine,
            boolean requestedDeliveryDateVisible,
            String requestedDeliveryDateDisplay,
            int productSubtotal,
            int shippingFee,
            int paperBagCount,
            int paperBagUnitPrice,
            long paperBagTotal,
            int totalAmount) {
        this.orderId = orderId;
        this.orderDateDisplay = orderDateDisplay;
        this.orderStatusDisplayName = orderStatusDisplayName;
        this.orderStatus = orderStatus;
        this.deliveryMethodDisplayName = deliveryMethodDisplayName;
        this.paymentMethodDisplayName = paymentMethodDisplayName;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        this.items = List.copyOf(items);
        this.deliveryAddressVisible = deliveryAddressVisible;
        this.shippingFeeVisible = shippingFeeVisible;
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.addressLine = addressLine;
        this.requestedDeliveryDateVisible = requestedDeliveryDateVisible;
        this.requestedDeliveryDateDisplay = requestedDeliveryDateDisplay;
        this.productSubtotal = productSubtotal;
        this.shippingFee = shippingFee;
        this.paperBagCount = paperBagCount;
        this.paperBagUnitPrice = paperBagUnitPrice;
        this.paperBagTotal = paperBagTotal;
        this.totalAmount = totalAmount;
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

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public String getDeliveryMethodDisplayName() {
        return deliveryMethodDisplayName;
    }

    public String getPaymentMethodDisplayName() {
        return paymentMethodDisplayName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public List<AdminOrderDetailItemView> getItems() {
        return items;
    }

    public boolean isDeliveryAddressVisible() {
        return deliveryAddressVisible;
    }

    public boolean isShippingFeeVisible() {
        return shippingFeeVisible;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public boolean isRequestedDeliveryDateVisible() {
        return requestedDeliveryDateVisible;
    }

    public String getRequestedDeliveryDateDisplay() {
        return requestedDeliveryDateDisplay;
    }

    public int getProductSubtotal() {
        return productSubtotal;
    }

    public int getShippingFee() {
        return shippingFee;
    }

    public int getPaperBagCount() {
        return paperBagCount;
    }

    public int getPaperBagUnitPrice() {
        return paperBagUnitPrice;
    }

    public long getPaperBagTotal() {
        return paperBagTotal;
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
