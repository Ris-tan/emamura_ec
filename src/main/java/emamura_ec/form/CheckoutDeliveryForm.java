package emamura_ec.form;

import org.springframework.util.StringUtils;

import emamura_ec.entity.DeliveryMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutDeliveryForm {

    @NotNull(message = "受取方法を選択してください。")
    private DeliveryMethod deliveryMethod;

    @Size(max = 50, message = "受取人氏名は50文字以内で入力してください。")
    private String recipientName;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください。")
    private String phoneNumber;

    @Pattern(regexp = "^$|\\d{3}-?\\d{4}$", message = "郵便番号は7桁、またはXXX-XXXX形式で入力してください。")
    private String postalCode;

    @Size(max = 10, message = "都道府県は10文字以内で入力してください。")
    private String prefecture;

    @Size(max = 255, message = "住所は255文字以内で入力してください。")
    private String addressLine;

    /*
     * Address fields cannot be globally @NotBlank because store pickup does not
     * need a delivery address. The conditional required rule is kept with the
     * form so that it runs before the service performs any delivery-area lookup.
     */
    @AssertTrue(message = "宅配便または自店配達を選択した場合は、配送先情報を入力してください。")
    public boolean isAddressInformationValid() {
        if (deliveryMethod == null || deliveryMethod == DeliveryMethod.STORE_PICKUP) {
            return true;
        }

        return StringUtils.hasText(recipientName)
                && StringUtils.hasText(phoneNumber)
                && StringUtils.hasText(postalCode)
                && StringUtils.hasText(prefecture)
                && StringUtils.hasText(addressLine);
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

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
}
