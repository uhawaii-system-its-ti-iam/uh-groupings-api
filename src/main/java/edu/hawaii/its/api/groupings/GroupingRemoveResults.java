package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

/**
 * GroupingRemoveResults shows the results after multiple UH affiliates have been removed from a group listing.
 */
public class GroupingRemoveResults implements GroupingResult {

    private final List<GroupingRemoveResult> groupingRemoveResults;
    private final RemoveMembersResults removeMembersResults;
    private String groupPath;
    private String resultCode;

    public GroupingRemoveResults(RemoveMembersResults removeMembersResults) {
        this.removeMembersResults = removeMembersResults;
        groupingRemoveResults = new ArrayList<>();
        for (RemoveMemberResult removeMemberResult : removeMembersResults.getResults()) {
            groupingRemoveResults.add(new GroupingRemoveResult(removeMemberResult));
        }
        setGroupPath(removeMembersResults.getGroupPath());
        setResultCode();
    }

    public GroupingRemoveResults() {
        groupingRemoveResults = new ArrayList<>();
        this.removeMembersResults = new RemoveMembersResults();
        setGroupPath("");
        setResultCode();
    }

    public void add(GroupingRemoveResult groupingRemoveResult) {
        groupingRemoveResults.add(groupingRemoveResult);
    }

    public void add(GroupingRemoveResults groupingRemoveResults) {
        for (GroupingRemoveResult result : groupingRemoveResults.getResults()) {
            add(result);
        }
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode() {
        for (RemoveMemberResult removeMemberResult : removeMembersResults.getResults()) {
            if (removeMemberResult.getResultCode().equals("SUCCESS")) {
                this.resultCode = "SUCCESS";
                return;
            }
        }
        this.resultCode = "FAILURE";

    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public List<GroupingRemoveResult> getResults() {
        return groupingRemoveResults;
    }
}
