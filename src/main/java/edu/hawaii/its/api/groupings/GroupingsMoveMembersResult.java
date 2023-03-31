package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

public class GroupingsMoveMembersResult implements GroupingsResult {
    private final GroupingsAddResults addResults;
    private final GroupingsRemoveResults removeResults;

    private String groupPath;

    private String resultCode;

    public GroupingsMoveMembersResult(AddMembersResults addMembersResults, RemoveMembersResults removeMembersResults) {
        addResults = new GroupingsAddResults(addMembersResults);
        removeResults = new GroupingsRemoveResults(removeMembersResults);
        setResultCode(addResults.getResultCode());
        setGroupPath(addResults.getGroupPath());
    }

    public GroupingsMoveMembersResult() {
        addResults = new GroupingsAddResults();
        removeResults = new GroupingsRemoveResults();
        setResultCode("");
        setResultCode("");
    }

    public GroupingsAddResults getAddResults() {
        return addResults;
    }

    public GroupingsRemoveResults getRemoveResults() {
        return removeResults;
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }
}
