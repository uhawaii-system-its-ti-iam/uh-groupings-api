package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.type.ManageSubjectResult;

import java.util.ArrayList;
import java.util.List;

public class ManageSubjectResults implements MemberResults<ManageSubjectResult> {
    private String resultCode;
    private List<ManageSubjectResult> results;

    public ManageSubjectResults() {
        setResults(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public ManageSubjectResults(List<ManageSubjectResult> results) {
        setResults(results);
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<ManageSubjectResult> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setResults(List<ManageSubjectResult> results) {
        this.results = results;
        setResultCode("SUCCESS");
    }
}