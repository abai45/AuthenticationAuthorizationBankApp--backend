package kz.group.reactAndSpring.enumeration;

public enum BankType {
    PREMIUM("Premium"),
    STANDART("Standart"),
    DEPOSIT("Deposit");

    private final String value;

    BankType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
