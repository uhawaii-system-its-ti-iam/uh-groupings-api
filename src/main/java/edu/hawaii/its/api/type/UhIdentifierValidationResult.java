package edu.hawaii.its.api.type;

import java.util.List;

import edu.hawaii.its.api.wrapper.Subject;

/**
 * The result of partitioning a list of submitted UH identifiers into those that resolved to a
 * valid Grouper subject and those that did not (malformed, or unknown to Grouper).
 */
public class UhIdentifierValidationResult {

    private final List<String> validIdentifiers;
    private final List<String> invalidIdentifiers;
    private final List<Subject> validSubjects;

    public UhIdentifierValidationResult(List<String> validIdentifiers, List<String> invalidIdentifiers,
            List<Subject> validSubjects) {
        this.validIdentifiers = validIdentifiers;
        this.invalidIdentifiers = invalidIdentifiers;
        this.validSubjects = validSubjects;
    }

    public List<String> getValidIdentifiers() {
        return validIdentifiers;
    }

    public List<String> getInvalidIdentifiers() {
        return invalidIdentifiers;
    }

    /**
     * The Grouper subject behind each valid identifier (one per distinct resolved subject, in the order its
     * identifier was submitted), so a caller that also needs subject attributes - not just the identifier - can
     * use the single bulk lookup this validation already made instead of querying Grouper again.
     */
    public List<Subject> getValidSubjects() {
        return validSubjects;
    }
}
