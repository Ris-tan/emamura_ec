package emamura_ec.entity;

public enum RibbonColor {
    // Persistence keeps the enum constant; displayName is intentionally UI-only.
    NONE("なし"),
    RED("赤"),
    BLUE("青"),
    PINK("ピンク"),
    GOLD("ゴールド"),
    SILVER("シルバー");

    private final String displayName;

    RibbonColor(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
