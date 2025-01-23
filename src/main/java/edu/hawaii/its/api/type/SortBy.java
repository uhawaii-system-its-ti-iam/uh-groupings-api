package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortBy {
    NAME("name", "name"),
    UID("uid", "search_string0"),
    UH_UUID("uhUuid", "subjectId");

    private final String value;
    private final String sortString;

    SortBy(String value, String sortString) {
        this.value = value;
        this.sortString = sortString;

    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonValue
    public String sortString() {
        return sortString;
    }

    public static SortBy find(String value) {
        for (SortBy type : SortBy.values()) {
            if (type.value().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
