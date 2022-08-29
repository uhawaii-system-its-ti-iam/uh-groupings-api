package edu.hawaii.its.api.type;

public enum InclusionType {

    IN("in"),
    OUT("out");

    private final String value;

    InclusionType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
