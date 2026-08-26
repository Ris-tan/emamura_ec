package emamura_ec.dto;

import java.util.List;

public class OrderDetailView {

    private final Long orderId;
    private final String orderDateDisplay;
    private final String orderStatusDisplayName;
    private final String deliveryMethodDisplayName;
    private final String paymentMethodDisplayName;
    private final List<OrderDetailItemView> items;
    private final boolean deliveryAddressVisible;
    private final String recipientName;
    private final String phoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String addressLine;
    private final int productSubtotal;
    private final int shippingFee;
    private final int paperBagCount;
    private final int paperBagUnitPrice;
    private final long paperBagTotal;
    private final int totalAmount;

    public OrderDetailView(
            Long orderId,
            String orderDateDisplay,
            String orderStatusDisplayName,
            String deliveryMethodDisplayName,
            String paymentMethodDisplayName,
            List<OrderDetailItemView> items,
            boolean deliveryAddressVisible,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String addressLine,
            int productSubtotal,
            int shippingFee,
            int paperBagCount,
            int paperBagUnitPrice,
            long paperBagTotal,
            int totalAmount) {
        this.orderId = orderId;
        this.orderDateDisplay = orderDateDisplay;
        this.orderStatusDisplayName = orderStatusDisplayName;
        this.deliveryMethodDisplayName = deliveryMethodDisplayName;
        this.paymentMethodDisplayName = paymentMethodDisplayName;
        this.items = List.copyOf(items);
        this.deliveryAddressVisible = deliveryAddressVisible;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.addressLine = addressLine;
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

    public String getDeliveryMethodDisplayName() {
        return deliveryMethodDisplayName;
    }

    public String getPaymentMethodDisplayName() {
        return paymentMethodDisplayName;
    }

    public List<OrderDetailItemView> getItems() {
        return items;
    }

    public boolean isDeliveryAddressVisible() {
        return deliveryAddressVisible;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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
