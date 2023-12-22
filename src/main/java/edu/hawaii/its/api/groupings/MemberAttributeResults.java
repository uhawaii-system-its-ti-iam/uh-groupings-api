package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import java.util.ArrayList;
import java.util.List;

public class MemberAttributeResults implements MemberResults<MemberResult> {

    private String resultCode;
    private List<MemberResult> results;

    public MemberAttributeResults() {
        setResultCode("FAILURE");
        setResults(new ArrayList<>());
    }

    public MemberAttributeResults(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setResults(subjectsResults.getSubjects());
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<MemberResult> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
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
