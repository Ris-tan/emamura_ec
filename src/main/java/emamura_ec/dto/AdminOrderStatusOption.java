package emamura_ec.dto;

import emamura_ec.entity.OrderStatus;

/**
 * A status value and its delivery-method-aware label for the admin select.
 * The persisted enum value stays separate from the wording shown to staff.
 */
public class AdminOrderStatusOption {

    private final OrderStatus value;
    private final String displayName;

    public AdminOrderStatusOption(OrderStatus value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public OrderStatus getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
