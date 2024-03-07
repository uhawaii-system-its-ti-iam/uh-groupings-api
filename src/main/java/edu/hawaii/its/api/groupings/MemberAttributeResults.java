package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import java.util.ArrayList;
import java.util.List;

public class MemberAttributeResults implements MemberResults<MemberResult> {

    private String resultCode;
    private List<String> invalid;
    private List<MemberResult> results;

    public MemberAttributeResults() {
        setResultCode("FAILURE");
        setResults(new ArrayList<>());
        setInvalid(new ArrayList<>());
    }

    public MemberAttributeResults(List<String> invalid) {
        setResultCode("FAILURE");
        setResults(new ArrayList<>());
        setInvalid(invalid);
    }

    public MemberAttributeResults(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setResults(subjectsResults.getSubjects());
        setInvalid(new ArrayList<>());
    }

    public String getResultCode() {
        return resultCode;
    }

    public List<String> getInvalid() {
        return invalid;
    }

    @Override
    public List<MemberResult> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setInvalid(List <String> invalid) {
        this.invalid = invalid;
    }

    private void setResults(List<Subject> subjects) {
        this.results = new ArrayList<>();
        for (Subject subject : subjects) {
            if (subject.getResultCode().equals("SUCCESS")) {
                this.results.add(new GroupingGroupMember(subject));
            }
        }
    }

}
