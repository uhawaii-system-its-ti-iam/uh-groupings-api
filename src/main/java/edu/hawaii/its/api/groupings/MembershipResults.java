package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.type.Membership;

public class MembershipResults implements MemberResults<Membership> {
    private String resultCode;
    private List<Membership> results;

    public MembershipResults() {
        setResults(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public MembershipResults(List<Membership> results) {
        setResults(results);
        setResultCode(resultCode);
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<Membership> getResults() {
        return results;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setResults(List<Membership> results) {
        this.results = results;
        setResultCode("SUCCESS");
    }
}
