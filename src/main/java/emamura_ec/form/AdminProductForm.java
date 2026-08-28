package emamura_ec.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AdminProductForm {

    @NotBlank(message = "商品名を入力してください。")
    @Size(max = 100, message = "商品名は100文字以内で入力してください。")
    private String productName;

    @NotNull(message = "カテゴリを選択してください。")
    @Positive(message = "カテゴリを選択してください。")
    private Long categoryId;

    @Size(max = 1000, message = "商品説明は1000文字以内で入力してください。")
    private String description;

    @NotNull(message = "価格を入力してください。")
    @Min(value = 0, message = "価格は0以上で入力してください。")
    private Integer price;

    @NotNull(message = "在庫数を入力してください。")
    @Min(value = 0, message = "在庫数は0以上で入力してください。")
    private Integer stock;

    @Size(max = 500, message = "商品画像パスは500文字以内で入力してください。")
    private String imageUrl;

    @NotNull(message = "販売状態を選択してください。")
    private Boolean isActive = Boolean.TRUE;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
