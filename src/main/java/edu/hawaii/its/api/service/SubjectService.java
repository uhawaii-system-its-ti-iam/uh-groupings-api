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
            throw new GrouperException("Grouper subject lookup failed");
        }
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
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
