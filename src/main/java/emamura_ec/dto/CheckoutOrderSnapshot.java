package emamura_ec.dto;

import java.util.List;

/**
 * A validated, current checkout snapshot. It is created again for both GET
 * confirmation and POST order placement instead of trusting old page values.
 */
public class CheckoutOrderSnapshot {

    private final List<CheckoutOrderItemData> items;
    private final boolean giftEnabled;
    private final CheckoutDeliveryData deliveryData;
    private final long productSubtotal;
    private final long shippingFee;
    private final int paperBagCount;
    private final int paperBagUnitPrice;
    private final long paperBagTotal;
    private final long total;

    public CheckoutOrderSnapshot(
            List<CheckoutOrderItemData> items,
            boolean giftEnabled,
            CheckoutDeliveryData deliveryData,
            long productSubtotal,
            long shippingFee,
            int paperBagCount,
            int paperBagUnitPrice,
            long paperBagTotal,
            long total) {
        this.items = List.copyOf(items);
        this.giftEnabled = giftEnabled;
        this.deliveryData = deliveryData;
        this.productSubtotal = productSubtotal;
        this.shippingFee = shippingFee;
        this.paperBagCount = paperBagCount;
        this.paperBagUnitPrice = paperBagUnitPrice;
        this.paperBagTotal = paperBagTotal;
        this.total = total;
    }

    public List<CheckoutOrderItemData> getItems() {
        return items;
    }

    public boolean isGiftEnabled() {
        return giftEnabled;
    }

    public CheckoutDeliveryData getDeliveryData() {
        return deliveryData;
    }

    public long getProductSubtotal() {
        return productSubtotal;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public int getPaperBagCount() {
        return paperBagCount;
    }

    public int getPaperBagUnitPrice() {
        return paperBagUnitPrice;
    }

    public long getPaperBagTotal() {
        return paperBagTotal;
    }

    public long getTotal() {
        return total;
    }
}
