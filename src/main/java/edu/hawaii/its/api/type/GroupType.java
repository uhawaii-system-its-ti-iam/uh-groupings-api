package edu.hawaii.its.api.type;

public enum GroupType {

    BASIS(":basis"),
    INCLUDE(":include"),
    EXCLUDE(":exclude"),
    OWNERS(":owners");

    private final String value;

    GroupType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
