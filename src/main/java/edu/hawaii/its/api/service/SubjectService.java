package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.exception.InvalidUhIdentifierException;
import edu.hawaii.its.api.type.UhIdentifierValidationResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * SubjectService provides a set of functions for checking the validity of UH identifiers.
 */
@Service
public class SubjectService {

    private static final Log logger = LogFactory.getLog(SubjectService.class);

    @Value("${groupings.api.success}")
    private String SUCCESS;

    private final GrouperService grouperService;

    @Value("${groupings.api.validation.uh-identifier.maxlength}")
    private int MAX_IDENTIFIER_LENGTH;

    @Value("${groupings.api.validation.uh-identifier.regex}")
    private String IDENTIFIER_REGEX;

    private static Pattern IDENTIFIER_PATTERN;

    public SubjectService(GrouperService grouperService) {
        this.grouperService = grouperService;
    }

    @PostConstruct
    public void init() {
        IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_REGEX);
    }

    public boolean isValidIdentifier(String currentUser, String uhIdentifier) {
        if (!isWellFormedIdentifier(uhIdentifier)) {
            logger.warn(String.format("Malformed path input rejected from currentUser: %s;", currentUser));
            throw new InvalidUhIdentifierException("Invalid UH identifier format");
        }
        return isValidSubject(getSubject(uhIdentifier));
    }

    private boolean isValidSubject(Subject subject) {
        return subject.getResultCode().equals(SUCCESS);
    }

    /**
     * Fetch all valid UH identifiers and return their corresponding UhUuids.
     */
    public List<String> getValidUhUuids(String currentUser, List<String> uhIdentifiers) {
        List<String> results = new ArrayList<>();
        List<String> wellFormed = uhIdentifiers.stream()
                .filter(this::isWellFormedIdentifier)
                .toList();
        if (wellFormed.size() != uhIdentifiers.size()) {
            logger.warn(String.format("Malformed path input rejected from currentUser: %s;", currentUser));
        }
        if (wellFormed.isEmpty()) {
            return results;
        }
        SubjectsResults subjectsResults = grouperService.getSubjects(wellFormed);
        if (!subjectsResults.isSuccessful()) {
            throw new GrouperException("Grouper subject lookup failed (rawResultCode=" + subjectsResults.getRawResultCode() + ")");
        }
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
    }

    /**
     * Partition uhIdentifiers, in a single bulk Grouper lookup, into those that resolve to a valid Grouper
     * subject and those that don't (malformed, or unknown to Grouper). Unlike getValidUhUuids, no identifier
     * is silently dropped: every invalid identifier is reported back, in full, in the order submitted (e.g. the
     * row order of an imported file), for the caller to display.
     */
    public UhIdentifierValidationResult validateUhIdentifiers(String currentUser, List<String> uhIdentifiers) {
        List<String> uniqueIdentifiers = uhIdentifiers.stream().distinct().toList();
        List<String> wellFormed = uniqueIdentifiers.stream().filter(this::isWellFormedIdentifier).toList();

        if (wellFormed.size() != uniqueIdentifiers.size()) {
            logger.warn(String.format("Malformed path input rejected from currentUser: %s;", currentUser));
        }

        Map<String, Subject> resolved = wellFormed.isEmpty() ? Map.of() : resolveSubjects(wellFormed);

        Set<String> validIdentifiers = new LinkedHashSet<>();
        Set<Subject> validSubjects = new LinkedHashSet<>();
        List<String> invalidIdentifiers = new ArrayList<>();
        for (String uhIdentifier : uniqueIdentifiers) {
            Subject subject = resolved.get(uhIdentifier);
            if (subject == null) {
                invalidIdentifiers.add(uhIdentifier);
                continue;
            }
            // A resolved subject is not guaranteed to carry a uhUuid (e.g. subjects sourced outside the standard
            // UH identifier system), in which case the submitted identifier is what gets used.
            String uhUuid = subject.getUhUuid();
            validIdentifiers.add(uhUuid.isEmpty() ? uhIdentifier : uhUuid);
            // Subject.equals() is by name/uid/uhUuid, so the same subject resolved from two submitted identifiers
            // (e.g. both a uid and its UH number) collapses to one entry here too.
            validSubjects.add(subject);
        }
        return new UhIdentifierValidationResult(
                new ArrayList<>(validIdentifiers), invalidIdentifiers, new ArrayList<>(validSubjects));
    }

    /**
     * Look up well-formed identifiers in one bulk Grouper call and return the subject each resolved identifier
     * belongs to; identifiers Grouper doesn't know are absent from the map.
     * <p>
     * Grouper does not answer with one entry per lookup: lookups that resolve to nothing are collapsed into a
     * single SUBJECT_NOT_FOUND entry, and lookups that resolve to the same subject can be too. The results are
     * therefore matched to the submitted identifiers by the uhUuid and uid they carry, never by position or count.
     */
    private Map<String, Subject> resolveSubjects(List<String> uhIdentifiers) {
        SubjectsResults subjectsResults = grouperService.getSubjects(uhIdentifiers);
        if (!subjectsResults.isSuccessful()) {
            throw new GrouperException(
                    "Grouper subject lookup failed (rawResultCode=" + subjectsResults.getRawResultCode() + ")");
        }

        Set<Subject> found = subjectsResults.getUnfilteredSubjects().stream()
                .filter(subject -> subject.getResultCode().startsWith(SUCCESS))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Subject> foundByKey = new HashMap<>();
        for (Subject subject : found) {
            foundByKey.put(lookupKey(subject.getUhUuid()), subject);
            foundByKey.put(lookupKey(subject.getUid()), subject);
        }
        foundByKey.remove("");

        Map<String, Subject> resolved = new HashMap<>();
        Set<Subject> matched = new HashSet<>();
        for (String uhIdentifier : uhIdentifiers) {
            Subject subject = foundByKey.get(lookupKey(uhIdentifier));
            if (subject != null) {
                resolved.put(uhIdentifier, subject);
                matched.add(subject);
            }
        }

        // A subject Grouper resolved but that answers to none of the submitted identifiers means one of them was
        // resolved under a form not recognised above. Verify the leftovers one at a time (a single lookup always
        // returns exactly one entry) rather than report a real member as not found.
        if (matched.size() < found.size()) {
            for (String uhIdentifier : uhIdentifiers) {
                if (!resolved.containsKey(uhIdentifier)) {
                    Subject subject = getSubject(uhIdentifier);
                    if (isValidSubject(subject)) {
                        resolved.put(uhIdentifier, subject);
                    }
                }
            }
        }
        return resolved;
    }

    private String lookupKey(String uhIdentifier) {
        return uhIdentifier == null ? "" : uhIdentifier.toLowerCase(Locale.ROOT);
    }

    public String getValidUhUuid(String currentUser, String uhIdentifier) {
        if (!isValidIdentifier(currentUser, uhIdentifier)) {
            return "";
        }
        Subject subject = getSubject(uhIdentifier);
        return subject.getUhUuid();
    }

    private boolean isWellFormedIdentifier(String uhIdentifier) {

        if (uhIdentifier == null || uhIdentifier.isEmpty()) {
            return false;
        }
        if (uhIdentifier.length() > MAX_IDENTIFIER_LENGTH) {
            return false;
        }
        return IDENTIFIER_PATTERN.matcher(uhIdentifier).matches();
    }

    private Subject getSubject(String uhIdentifier) {
        SubjectsResults subjectsResults = grouperService.getSubjects(uhIdentifier);
        if (!subjectsResults.isSuccessful()) {
            throw new GrouperException("Grouper subject lookup failed");
        }

        List<Subject> subjects = subjectsResults.getSubjects();
        if (!subjects.isEmpty()) {
            Subject subject = subjects.get(0);
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                return new Subject();
            }
            return subject;
        }

        return new Subject();
    }
}
