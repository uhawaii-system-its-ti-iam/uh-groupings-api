package edu.hawaii.its.api.type;

public enum PrivilegeType {

    IN("optin", InclusionType.IN),
    OUT("optout", InclusionType.OUT);

    private final String value;
    private final InclusionType inclusionType;

    PrivilegeType(String value, InclusionType inclusionType) {
        this.value = value;
        this.inclusionType = inclusionType;
    }

    public InclusionType inclusionType() {
        return inclusionType;
    }

    public String value() {
        return value;
    }
}
