package emamura_ec.dto;

import emamura_ec.entity.DeliveryMethod;

public class CheckoutDeliveryData {

    private final DeliveryMethod deliveryMethod;
    private final String recipientName;
    private final String phoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String cityAddress;
    private final String addressDetail;
    private final Integer shippingFee;
    private final Integer leadDays;

    public CheckoutDeliveryData(
            DeliveryMethod deliveryMethod,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String cityAddress,
            String addressDetail,
            Integer shippingFee,
            Integer leadDays) {
        this.deliveryMethod = deliveryMethod;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.cityAddress = cityAddress;
        this.addressDetail = addressDetail;
        this.shippingFee = shippingFee;
        this.leadDays = leadDays;
    }

    /**
     * Keeps callers using the previous combined address representation source-compatible.
     * New checkout data should use the constructor with cityAddress and addressDetail.
     */
    public CheckoutDeliveryData(
            DeliveryMethod deliveryMethod,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String addressLine,
            Integer shippingFee,
            Integer leadDays) {
        this(deliveryMethod, recipientName, phoneNumber, postalCode, prefecture,
                addressLine, null, shippingFee, leadDays);
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

    public String getCityAddress() {
        return cityAddress;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    /**
     * order_addresses.address_line remains one column. The checkout DTO keeps
     * the UI parts separate and exposes the future persistence format here.
     */
    public String getAddressLine() {
        if (cityAddress == null) {
            return addressDetail;
        }
        if (addressDetail == null) {
            return cityAddress;
        }
        return cityAddress + addressDetail;
    }

    public Integer getShippingFee() {
        return shippingFee;
    }

    public Integer getLeadDays() {
        return leadDays;
    }
}
