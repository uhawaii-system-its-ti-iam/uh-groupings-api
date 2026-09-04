package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
     * is silently dropped: every invalid identifier is reported back, in full, for the caller to display.
     * <p>
     * Results are correlated back to the originally submitted identifier by request position rather than by
     * any identifier Grouper echoes in the response, since a resolved subject is not guaranteed to carry a
     * uhUuid (e.g. subjects sourced outside the standard UH identifier system).
     */
    public UhIdentifierValidationResult validateUhIdentifiers(String currentUser, List<String> uhIdentifiers) {
        List<String> uniqueIdentifiers = uhIdentifiers.stream().distinct().toList();

        List<String> invalidIdentifiers = new ArrayList<>();
        List<String> wellFormed = new ArrayList<>();

        for (String uhIdentifier : uniqueIdentifiers) {
            if (isWellFormedIdentifier(uhIdentifier)) {
                wellFormed.add(uhIdentifier);
            } else {
                invalidIdentifiers.add(uhIdentifier);
            }
        }

        if (!invalidIdentifiers.isEmpty()) {
            logger.warn(String.format("Malformed path input rejected from currentUser: %s;", currentUser));
        }

        List<String> validIdentifiers = new ArrayList<>();

        if (!wellFormed.isEmpty()) {
            SubjectsResults subjectsResults = grouperService.getSubjects(wellFormed);
            if (!subjectsResults.isSuccessful()) {
                throw new GrouperException(
                        "Grouper subject lookup failed (rawResultCode=" + subjectsResults.getRawResultCode() + ")");
            }

            List<Subject> subjects = subjectsResults.getSubjectsInRequestOrder();
            if (subjects.size() != wellFormed.size()) {
                throw new GrouperException("Grouper subject lookup returned an unexpected number of results");
            }

            for (int i = 0; i < wellFormed.size(); i++) {
                String uhIdentifier = wellFormed.get(i);
                Subject subject = subjects.get(i);

                if (!subject.getResultCode().startsWith(SUCCESS)) {
                    invalidIdentifiers.add(uhIdentifier);
                    continue;
                }
                
                String uhUuid = subject.getUhUuid();
                validIdentifiers.add(uhUuid.isEmpty() ? uhIdentifier : uhUuid);
            }
        }
        return new UhIdentifierValidationResult(validIdentifiers, invalidIdentifiers);
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
