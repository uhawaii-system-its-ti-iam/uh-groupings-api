

package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * SubjectService provides a set of functions for checking the validity of UH identifiers.
 */
@Service
public class SubjectService {
    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Autowired
    private GrouperApiService grouperApiService;

    public Person getPerson(String uhIdentifier) {
        Subject subject = getSubject(uhIdentifier);
        if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
            return new Person();
        }
        return new Person(subject.getName(), subject.getUhUuid(), subject.getUid(), subject.getFirstName(),
                subject.getLastName());
    }

    public boolean isValidIdentifier(String uhIdentifier) {
        return isValidSubject(getSubject(uhIdentifier));
    }

    private boolean isValidSubject(Subject subject) {
        return subject.getResultCode().equals(SUCCESS);
    }

    /**
     * Fetch all valid UH identifiers and return their corresponding UhUuids.
     */
    public List<String> getValidUhUuids(List<String> uhIdentifiers) {
        SubjectsResults subjectsResults = grouperApiService.getSubjects(uhIdentifiers);
        List<String> results = new ArrayList<>();
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
    }

    public String getValidUhUuid(String uhIdentifier) {
        Subject subject = getSubject(uhIdentifier);
        if (!isValidSubject(subject)) {
            return "";
        }
        return subject.getUhUuid();
    }

    private Subject getSubject(String uhIdentifier) {
        SubjectsResults subjectsResults = grouperApiService.getSubjects(uhIdentifier);
        if (subjectsResults == null) {
            return new Subject();
        }
        List<Subject> subjects = subjectsResults.getSubjects();
        if (subjects.size() == 1) {
            return subjects.get(0);
        }
        return new Subject();
    }
}
