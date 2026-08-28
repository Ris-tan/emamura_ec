package emamura_ec.form;

import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GiftItemForm {

    @NotNull(message = "商品情報が不正です。")
    private Long productId;

    // These display fields are rebuilt from the current cart and are never trusted for persistence.
    private Integer quantity;
    private String productName;
    private String imageUrl;

    private boolean wrappingEnabled;
    private WrappingType wrappingType = WrappingType.NONE;

    private boolean ribbonEnabled;
    private RibbonColor ribbonColor = RibbonColor.NONE;

    private boolean messageEnabled;

    @Size(max = 30, message = "メッセージカードは30文字以内で入力してください。")
    private String messageText;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isWrappingEnabled() {
        return wrappingEnabled;
    }

    public void setWrappingEnabled(boolean wrappingEnabled) {
        this.wrappingEnabled = wrappingEnabled;
    }

    public WrappingType getWrappingType() {
        return wrappingType;
    }

    public void setWrappingType(WrappingType wrappingType) {
        this.wrappingType = wrappingType;
    }

    public boolean isRibbonEnabled() {
        return ribbonEnabled;
    }

    public void setRibbonEnabled(boolean ribbonEnabled) {
        this.ribbonEnabled = ribbonEnabled;
    }

    public RibbonColor getRibbonColor() {
        return ribbonColor;
    }

    public void setRibbonColor(RibbonColor ribbonColor) {
        this.ribbonColor = ribbonColor;
    }

    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    public void setMessageEnabled(boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
