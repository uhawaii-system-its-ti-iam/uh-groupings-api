package edu.hawaii.its.api.type;

public enum PreferenceType {

    ENABLE("enable", true),
    DISABLE("disable", false);

    private final String value;

    private final boolean toggle;

    PreferenceType(String value, boolean toggle) {
        this.value = value;
        this.toggle = toggle;
    }

    public String value() {
        return value;
    }

    public boolean toggle() {
        return toggle;
    }

    public static PreferenceType find(String value) {
        for (PreferenceType type : PreferenceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
