package emamura_ec.dto;

import emamura_ec.entity.DeliveryMethod;

public class CheckoutDeliveryData {

    private final DeliveryMethod deliveryMethod;
    private final String recipientName;
    private final String phoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String addressLine;
    private final Integer shippingFee;
    private final Integer leadDays;

    public CheckoutDeliveryData(
            DeliveryMethod deliveryMethod,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String addressLine,
            Integer shippingFee,
            Integer leadDays) {
        this.deliveryMethod = deliveryMethod;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.addressLine = addressLine;
        this.shippingFee = shippingFee;
        this.leadDays = leadDays;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
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

    public Integer getShippingFee() {
        return shippingFee;
    }

    public Integer getLeadDays() {
        return leadDays;
    }
}
