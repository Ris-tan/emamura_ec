package emamura_ec.dto;

public class AdminProductView {

    private final Long productId;
    private final String productName;
    private final String categoryName;
    private final Integer price;
    private final Integer stock;
    private final String imageUrl;
    private final Boolean isActive;

    public AdminProductView(
            Long productId,
            String productName,
            String categoryName,
            Integer price,
            Integer stock,
            String imageUrl,
            Boolean isActive) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }
}
