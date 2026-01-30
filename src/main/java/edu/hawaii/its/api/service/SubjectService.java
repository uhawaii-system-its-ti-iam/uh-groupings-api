package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.hawaii.its.api.exception.GrouperException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.InvalidUhIdentifierException;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * SubjectService provides a set of functions for checking the validity of UH identifiers.
 */
@Service
public class SubjectService {
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

    public boolean isValidIdentifier(String uhIdentifier) {
        if (!isWellFormedIdentifier(uhIdentifier)) {
            return false;
        }
        return isValidSubject(getSubject(uhIdentifier));
    }

    private boolean isValidSubject(Subject subject) {
        return subject.getResultCode().equals(SUCCESS);
    }

    /**
     * Fetch all valid UH identifiers and return their corresponding UhUuids.
     */
    public List<String> getValidUhUuids(List<String> uhIdentifiers) {
        List<String> results = new ArrayList<>();
        List<String> wellFormed = uhIdentifiers.stream()
                .filter(this::isWellFormedIdentifier)
                .toList();
        if (wellFormed.isEmpty()) {
            return results;
        }
        SubjectsResults subjectsResults = grouperService.getSubjects(wellFormed);
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
    }

    public String getValidUhUuid(String uhIdentifier) {
        if (!isWellFormedIdentifier(uhIdentifier)) {
            return "";
        }
        Subject subject = getSubject(uhIdentifier);
        if (!isValidSubject(subject)) {
            return "";
        }
        return subject.getUhUuid();
    }

    private boolean isWellFormedIdentifier(String uhIdentifier) {
        if (uhIdentifier == null || uhIdentifier.isEmpty()) {
            return false;
        }
        if (uhIdentifier.length() > MAX_IDENTIFIER_LENGTH) {
            return false;
        }
        if (IDENTIFIER_PATTERN == null) {
            IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_REGEX);
        }
        return IDENTIFIER_PATTERN.matcher(uhIdentifier).matches();
    }

    private Subject getSubject(String uhIdentifier) {
        try {
            SubjectsResults subjectsResults = grouperService.getSubjects(uhIdentifier);
            List<Subject> subjects = subjectsResults.getSubjects();

            if (subjectsResults.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                return new Subject();
            }
            if (subjects.size() >= 1) {
                return subjects.get(0);
            }
            return new Subject();
        } catch (GrouperException e) {
            return new Subject();
        }
    }
}
