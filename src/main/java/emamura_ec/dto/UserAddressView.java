package emamura_ec.dto;

public class UserAddressView {

    private final Long userAddressId;
    private final String recipientName;
    private final String phoneNumber;
    private final String postalCode;
    private final String prefecture;
    private final String addressLine;
    private final boolean defaultAddress;

    public UserAddressView(
            Long userAddressId,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String prefecture,
            String addressLine,
            boolean defaultAddress) {
        this.userAddressId = userAddressId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.addressLine = addressLine;
        this.defaultAddress = defaultAddress;
    }

    public Long getUserAddressId() {
        return userAddressId;
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

    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}
