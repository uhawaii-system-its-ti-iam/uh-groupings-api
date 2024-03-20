package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.type.ManagePersonResult;

import java.util.ArrayList;
import java.util.List;

public class ManagePersonResults implements MemberResults<ManagePersonResult> {
    private String resultCode;
    private List<ManagePersonResult> results;

    public ManagePersonResults() {
        setResults(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public ManagePersonResults(List<ManagePersonResult> results) {
        setResults(results);
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<ManagePersonResult> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setResults(List<ManagePersonResult> results) {
        this.results = results;
        setResultCode("SUCCESS");
    }
}