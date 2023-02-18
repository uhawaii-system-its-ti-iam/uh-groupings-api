package edu.hawaii.its.api.type;

public enum PreferenceStatus {
    // PreferenceStatus

    ENABLE("enable", true),
    DISABLE("disable", false);

    private final String value;

    private final boolean toggle;

    PreferenceStatus(String value, boolean toggle) {
        this.value = value;
        this.toggle = toggle;
    }

    public String value() {
        return value;
    }

    public boolean toggle() {
        return toggle;
    }

    public static PreferenceStatus find(String value) {
        for (PreferenceStatus type : PreferenceStatus.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
