package edu.hawaii.its.api.type;

public enum OptType {

    IN("uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-in", InclusionType.IN),
    OUT("uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-out", InclusionType.OUT);

    private final String value;
    private final InclusionType inclusionType;

    OptType(String value, InclusionType inclusionType) {
        this.value = value;
        this.inclusionType = inclusionType;
    }

    public String value() {
        return value;
    }

    public InclusionType inclusionType() {
        return inclusionType;
    }

    public static OptType find(String value) {
        for (OptType type : OptType.values()) {
            if (type.value().equals(value)) {
                return type;
            }
        }
        return null;
    }
}