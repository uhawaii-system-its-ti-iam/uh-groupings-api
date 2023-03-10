package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;

import java.util.ArrayList;
import java.util.List;

public class GroupingsRemoveResults implements GroupingsResult {

    private final List<GroupingsRemoveResult> groupingsRemoveResults;
    private final RemoveMembersResults removeMembersResults;
    private String groupPath;
    private String resultCode;

    public GroupingsRemoveResults(RemoveMembersResults removeMembersResults) {
        this.removeMembersResults = removeMembersResults;
        groupingsRemoveResults = new ArrayList<>();
        for (RemoveMemberResult removeMemberResult : removeMembersResults.getResults()) {
            groupingsRemoveResults.add(new GroupingsRemoveResult(removeMemberResult));
        }
        setGroupPath(removeMembersResults.getGroupPath());
        setResultCode();
    }

    public GroupingsRemoveResults() {
        groupingsRemoveResults = new ArrayList<>();
        this.removeMembersResults = new RemoveMembersResults();
        setGroupPath("");
        setResultCode();
    }

    public void add(GroupingsRemoveResult groupingsRemoveResult) {
        groupingsRemoveResults.add(groupingsRemoveResult);
    }

    public void add(GroupingsRemoveResults groupingsRemoveResults) {
        for (GroupingsRemoveResult result : groupingsRemoveResults.getResults()) {
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

    public List<GroupingsRemoveResult> getResults() {
        return groupingsRemoveResults;
    }
}
