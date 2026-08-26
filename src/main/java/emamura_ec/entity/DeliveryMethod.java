package emamura_ec.entity;

public enum DeliveryMethod {
    SHIPPING("お届け"),
    LOCAL_DELIVERY("自店配達"),
    STORE_PICKUP("店頭受取");

    // Keep persisted enum values stable while allowing user-facing labels to change independently.
    private final String displayName;

    DeliveryMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
