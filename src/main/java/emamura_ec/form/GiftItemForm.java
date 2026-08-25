package emamura_ec.form;

import emamura_ec.entity.RibbonColor;
import emamura_ec.entity.WrappingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GiftItemForm {

    @NotNull(message = "商品情報が不正です。")
    private Long productId;

    // These display fields are rebuilt from the current cart. They are never used as the source of truth for validation or persistence.
    private Integer quantity;

    private String productName;

    private String imageUrl;

    @NotNull(message = "ラッピングを選択してください。")
    private WrappingType wrappingType;

    @NotNull(message = "リボンカラーを選択してください。")
    private RibbonColor ribbonColor;

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

    public WrappingType getWrappingType() {
        return wrappingType;
    }

    public void setWrappingType(WrappingType wrappingType) {
        this.wrappingType = wrappingType;
    }

    public RibbonColor getRibbonColor() {
        return ribbonColor;
    }

    public void setRibbonColor(RibbonColor ribbonColor) {
        this.ribbonColor = ribbonColor;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
