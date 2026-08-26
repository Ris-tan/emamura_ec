package emamura_ec.entity;

public enum OrderStatus {
    PENDING("注文受付"),
    CONFIRMED("注文確定"),
    SHIPPED("発送済み"),
    DELIVERED("配達完了"),
    CANCELLED("キャンセル");

    // The database stores the enum constant; screens use this separate label.
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
