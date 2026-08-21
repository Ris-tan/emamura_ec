package emamura_ec.dto;

import emamura_ec.entity.Product;

// Product Entityにないセッション数量と表示用小計を組み合わせる、カート画面専用の値です。
public class CartItemView {

    private final Product product;
    private final int quantity;
    private final long subtotal;

    public CartItemView(Product product, int quantity, long subtotal) {
        this.product = product;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return subtotal;
    }
}
