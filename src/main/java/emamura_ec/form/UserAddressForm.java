package emamura_ec.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserAddressForm {

    @NotBlank(message = "受取人氏名を入力してください。")
    @Size(max = 50, message = "受取人氏名は50文字以内で入力してください。")
    private String recipientName;

    @NotBlank(message = "電話番号を入力してください。")
    @Size(max = 20, message = "電話番号は20文字以内で入力してください。")
    private String phoneNumber;

    @NotBlank(message = "郵便番号を入力してください。")
    @Pattern(regexp = "^\\d{3}-?\\d{4}$", message = "郵便番号は7桁、またはXXX-XXXX形式で入力してください。")
    private String postalCode;

    @NotBlank(message = "都道府県を入力してください。")
    @Size(max = 10, message = "都道府県は10文字以内で入力してください。")
    private String prefecture;

    @NotBlank(message = "住所を入力してください。")
    @Size(max = 255, message = "住所は255文字以内で入力してください。")
    private String addressLine;

    private boolean defaultAddress;

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}
