package emamura_ec.dto;

import java.util.List;

public class CartView {

    private final List<CartItemView> items;
    private final long total;

    public CartView(List<CartItemView> items, long total) {
        this.items = items;
        this.total = total;
    }

    public List<CartItemView> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }
}
