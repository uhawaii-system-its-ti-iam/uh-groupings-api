package edu.hawaii.its.api.service;

import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import java.util.ArrayList;
import java.util.List;

public class SubjectsService {
    private final SubjectsResults subjectsResults;

    public SubjectsService(SubjectsResults subjectsResults) {
        this.subjectsResults = subjectsResults;
    }

    /**
     * Throws an exception if any subjects were not found.
     */
    SubjectsService check() {
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                // Todo this needs to be spoken about.
                throw new UhMemberNotFoundException(JsonUtil.asJson(invalidUhUuids()));
            }
        }
        return this;
    }

    public List<String> validUhUuids() {
        List<String> results = new ArrayList<>();
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUBJECT_NOT_FOUND")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
    }

    public List<String> invalidUhUuids() {
        List<String> results = new ArrayList<>();
        for (Subject subject : subjectsResults.getSubjects()) {
            if (subject.getResultCode().equals("SUCCESS")) {
                continue;
            }
            results.add(subject.getUhUuid());
        }
        return results;
    }
}
