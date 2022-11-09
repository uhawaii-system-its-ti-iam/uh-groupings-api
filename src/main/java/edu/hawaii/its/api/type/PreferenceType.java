package edu.hawaii.its.api.type;

public enum PreferenceType {

    ENABLE("enable", true),
    DISABLE("disable", false);

    private final String value;

    private final boolean toggleOn;

    PreferenceType(String value, boolean toggleOn) {
        this.value = value;
        this.toggleOn = toggleOn;
    }

    public String value() {
        return value;
    }

    public boolean toggleOn() {
        return toggleOn;
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
