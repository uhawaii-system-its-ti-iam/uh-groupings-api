package edu.hawaii.its.api.type;

import java.util.List;

/**
 * The result of partitioning a list of submitted UH identifiers into those that resolved to a
 * valid Grouper subject and those that did not (malformed, or unknown to Grouper).
 */
public class UhIdentifierValidationResult {

    private final List<String> validIdentifiers;
    private final List<String> invalidIdentifiers;

    public UhIdentifierValidationResult(List<String> validIdentifiers, List<String> invalidIdentifiers) {
        this.validIdentifiers = validIdentifiers;
        this.invalidIdentifiers = invalidIdentifiers;
    }

    public List<String> getValidIdentifiers() {
        return validIdentifiers;
    }

    public List<String> getInvalidIdentifiers() {
        return invalidIdentifiers;
    }
}
