package emamura_ec.entity;

public enum WrappingType {
    // Persistence keeps the enum constant; displayName is intentionally UI-only.
    NONE("なし"),
    GIFT_WRAP("ギフトラッピング");

    private final String displayName;

    WrappingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
