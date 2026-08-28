package emamura_ec.entity;

public enum OrderStatus {
    PENDING("注文受付済み"),
    PREPARING("準備中"),
    READY_FOR_PICKUP("受取準備完了"),
    CONFIRMED("注文確定"),
    SHIPPED("発送済み"),
    DELIVERED("配達完了"),
    COMPLETED("完了"),
    CANCELLED("キャンセル");

    // Keep legacy values such as CONFIRMED and DELIVERED so existing STRING data remains readable.
    // The database stores the enum constant; screens use this separate label.
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
