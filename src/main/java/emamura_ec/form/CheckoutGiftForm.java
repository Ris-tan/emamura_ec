package emamura_ec.form;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CheckoutGiftForm {

    // One item represents one cart line, so all units of the same product share these gift settings.
    @Valid
    private List<GiftItemForm> items = new ArrayList<>();

    @NotNull(message = "紙袋数量を入力してください。")
    @Min(value = 0, message = "紙袋数量は0枚以上で入力してください。")
    private Integer paperBagCount = 0;

    public CheckoutGiftForm() {
    }

    public CheckoutGiftForm(List<GiftItemForm> items, Integer paperBagCount) {
        this.items = items;
        this.paperBagCount = paperBagCount;
    }

    public List<GiftItemForm> getItems() {
        return items;
    }

    public void setItems(List<GiftItemForm> items) {
        this.items = items;
    }

    public Integer getPaperBagCount() {
        return paperBagCount;
    }

    public void setPaperBagCount(Integer paperBagCount) {
        this.paperBagCount = paperBagCount;
    }
}
