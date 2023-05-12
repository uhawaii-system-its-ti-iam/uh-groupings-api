package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

/**
 * GroupingsMoveMemberResult holds the result data on when listed group members are moved from include to exclude
 * visa-versa.
 */
public class GroupingMoveMembersResult implements GroupingResult {
    private final GroupingAddResults addResults;
    private final GroupingRemoveResults removeResults;

    private String groupPath;

    private String resultCode;

    public GroupingMoveMembersResult(AddMembersResults addMembersResults, RemoveMembersResults removeMembersResults) {
        addResults = new GroupingAddResults(addMembersResults);
        removeResults = new GroupingRemoveResults(removeMembersResults);
        setResultCode(addResults.getResultCode());
        setGroupPath(addResults.getGroupPath());
    }

    public GroupingMoveMembersResult() {
        addResults = new GroupingAddResults();
        removeResults = new GroupingRemoveResults();
        setResultCode("");
        setResultCode("");
    }

    public GroupingAddResults getAddResults() {
        return addResults;
    }

    public GroupingRemoveResults getRemoveResults() {
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
