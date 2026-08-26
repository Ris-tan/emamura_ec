package emamura_ec.entity;

public enum PaymentMethod {
    CREDIT_CARD("クレジットカード"),
    BANK_TRANSFER("銀行振込"),
    CONVENIENCE_STORE("コンビニ払い"),
    CASH_ON_DELIVERY("代金引換");

    // The database stores the enum constant; screens use this separate label.
    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
