package edu.hawaii.its.api.type;

public enum GroupType {

    INCLUDE(":include"),
    EXCLUDE(":exclude");

    private final String value;

    GroupType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static GroupType find(String value) {
        for (GroupType type : GroupType.values()) {
            if (type.value().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
