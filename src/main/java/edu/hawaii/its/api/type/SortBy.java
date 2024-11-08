package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortBy {

    NAME("name", "name"),
    UID("uid", "sort_string0"),
    UH_UUID("uhUuid", "subjectId");

//    IN("uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-in", InclusionType.IN),
//    OUT("uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-out", InclusionType.OUT);

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
