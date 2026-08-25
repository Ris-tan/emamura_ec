package emamura_ec.dto;

import java.util.List;

public class CheckoutGiftData {

    private final List<CheckoutGiftItemData> items;
    private final Integer paperBagCount;

    public CheckoutGiftData(List<CheckoutGiftItemData> items, Integer paperBagCount) {
        this.items = List.copyOf(items);
        this.paperBagCount = paperBagCount;
    }

    public List<CheckoutGiftItemData> getItems() {
        return items;
    }

    public Integer getPaperBagCount() {
        return paperBagCount;
    }
}
