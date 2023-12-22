package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

public class InvalidUhIdentifiersResults implements MemberResults<String> {

    private String resultCode;
    private List<String> results;

    public InvalidUhIdentifiersResults() {
        this.results = new ArrayList<>();
        this.resultCode = "FAILURE";
    }

    public InvalidUhIdentifiersResults(List<String> results) {
        setResults(results);
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<String> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setResults(List<String> results) {
        this.results = results;
        setResultCode("SUCCESS");
    }
}
