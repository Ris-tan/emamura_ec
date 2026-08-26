package emamura_ec.dto;

import java.util.List;

public class CheckoutConfirmView {

    private final List<CheckoutConfirmItemView> items;
    private final boolean giftEnabled;
    private final String deliveryMethodDisplayName;
    private final boolean deliveryAddressVisible;
    private final String recipientName;
    private final String phoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String cityAddress;
    private final String addressDetail;
    private final long productSubtotal;
    private final long shippingFee;
    private final int paperBagCount;
    private final long paperBagUnitPrice;
    private final long paperBagTotal;
    private final long total;

    public CheckoutConfirmView(
            List<CheckoutConfirmItemView> items,
            boolean giftEnabled,
            String deliveryMethodDisplayName,
            boolean deliveryAddressVisible,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String cityAddress,
            String addressDetail,
            long productSubtotal,
            long shippingFee,
            int paperBagCount,
            long paperBagUnitPrice,
            long paperBagTotal,
            long total) {
        this.items = List.copyOf(items);
        this.giftEnabled = giftEnabled;
        this.deliveryMethodDisplayName = deliveryMethodDisplayName;
        this.deliveryAddressVisible = deliveryAddressVisible;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.cityAddress = cityAddress;
        this.addressDetail = addressDetail;
        this.productSubtotal = productSubtotal;
        this.shippingFee = shippingFee;
        this.paperBagCount = paperBagCount;
        this.paperBagUnitPrice = paperBagUnitPrice;
        this.paperBagTotal = paperBagTotal;
        this.total = total;
    }

    public List<CheckoutConfirmItemView> getItems() {
        return items;
    }

    public boolean isGiftEnabled() {
        return giftEnabled;
    }

    public String getDeliveryMethodDisplayName() {
        return deliveryMethodDisplayName;
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

    public String getCityAddress() {
        return cityAddress;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public long getProductSubtotal() {
        return productSubtotal;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public int getPaperBagCount() {
        return paperBagCount;
    }

    public long getPaperBagUnitPrice() {
        return paperBagUnitPrice;
    }

    public long getPaperBagTotal() {
        return paperBagTotal;
    }

    public long getTotal() {
        return total;
    }
}
