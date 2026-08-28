package emamura_ec.form;

import java.time.LocalDate;

import org.springframework.util.StringUtils;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutDeliveryForm {

    @NotBlank(message = "受取方法を選択してください。")
    private String deliveryOption;

    @Size(max = 50, message = "受取人氏名は50文字以内で入力してください。")
    private String recipientName;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください。")
    private String phoneNumber;

    @Pattern(regexp = "^$|\\d{3}-?\\d{4}$", message = "郵便番号は7桁、またはXXX-XXXX形式で入力してください。")
    private String postalCode;

    @Size(max = 10, message = "都道府県は10文字以内で入力してください。")
    private String prefecture;

    @Size(max = 255, message = "市区町村・町域は255文字以内で入力してください。")
    private String cityAddress;

    @Size(max = 255, message = "番地・建物名は255文字以内で入力してください。")
    private String addressDetail;

    @Positive(message = "保存済みのお届け先を選択してください。")
    private Long savedAddressId;

    /**
     * The desired date is optional. The delivery service validates it against
     * the current delivery area's lead time because an HTML min attribute can
     * be bypassed or become stale before the order is placed.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requestedDeliveryDate;

    /*
     * Address fields cannot be globally @NotBlank because store pickup does not
     * need a delivery address. Recipient information remains required for both
     * options because it identifies the person receiving the order.
     */
    @AssertTrue(message = "受取人氏名と電話番号を入力してください。お届けの場合は住所情報も入力してください。")
    public boolean isAddressInformationValid() {
        if (!StringUtils.hasText(deliveryOption)) {
            return true;
        }

        boolean recipientInformationValid = StringUtils.hasText(recipientName)
                && StringUtils.hasText(phoneNumber);
        if ("STORE_PICKUP".equals(deliveryOption)) {
            return recipientInformationValid;
        }

        // When a saved address is selected, the server replaces all address fields from the owned DB record.
        if (savedAddressId != null) {
            return true;
        }

        return recipientInformationValid
                && StringUtils.hasText(postalCode)
                && StringUtils.hasText(prefecture)
                && StringUtils.hasText(cityAddress)
                && StringUtils.hasText(addressDetail);
    }

    public String getDeliveryOption() {
        return deliveryOption;
    }

    public void setDeliveryOption(String deliveryOption) {
        this.deliveryOption = deliveryOption;
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

    public String getCityAddress() {
        return cityAddress;
    }

    public void setCityAddress(String cityAddress) {
        this.cityAddress = cityAddress;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public Long getSavedAddressId() {
        return savedAddressId;
    }

    public void setSavedAddressId(Long savedAddressId) {
        this.savedAddressId = savedAddressId;
    }

    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }
}
