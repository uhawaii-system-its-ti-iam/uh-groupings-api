package edu.hawaii.its.api.util;

public enum GrouperAttributes {
    NUMBER("uhUuid"),
    USERNAME("uid"),
    COMMON_NAME("cn"),
    FIRST_NAME("givenName"),
    LAST_NAME("sn");
    private final String value;

    GrouperAttributes(String value) {
        this.value = value;
    }
    public String value() {
        return value;
    }
}
