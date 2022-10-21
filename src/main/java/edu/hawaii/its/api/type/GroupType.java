package edu.hawaii.its.api.type;

import java.util.List;

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

    public boolean areGroups(List<String> paths) {
        for (String path : paths) {
            if (!path.endsWith(INCLUDE.value()) && !path.endsWith(EXCLUDE.value()) && !path.endsWith(
                    OWNERS.value())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGroup(String path) {
        return !(!path.endsWith(INCLUDE.value()) && !path.endsWith(EXCLUDE.value()) && !path.endsWith(OWNERS.value()));
    }
}
