package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.RemoveResult;

import java.util.ArrayList;
import java.util.List;

public class GroupingsRemoveResults extends GroupingsMembersResults implements MemberResults<GroupingsRemoveResult> {

    private final List<GroupingsRemoveResult> groupingsRemoveResults;

    public GroupingsRemoveResults(RemoveMembersResults removeMembersResults) {
        groupingsRemoveResults = new ArrayList<>();
        groupPath = removeMembersResults.getGroupPath();
        for (RemoveResult removeResult : removeMembersResults.getResults()) {
            groupingsRemoveResults.add(new GroupingsRemoveResult(removeResult));
        }
    }

    public GroupingsRemoveResults() {
        groupingsRemoveResults = new ArrayList<>();
    }

    public void add(GroupingsRemoveResult groupingsRemoveResult) {
        groupingsRemoveResults.add(groupingsRemoveResult);
    }


    public void add(GroupingsRemoveResults groupingsRemoveResults){
        for(GroupingsRemoveResult result: groupingsRemoveResults.getResults()){
            add(result);
        }
    }

    @Override
    public String getResultCode() {
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (GroupingsRemoveResult groupingsRemoveResult : groupingsRemoveResults) {
            if (groupingsRemoveResult.resultCode.equals(success)) {
                return success;
            }
        }
        return failure;
    }

    @Override
    public List<GroupingsRemoveResult> getResults() {
        return groupingsRemoveResults;
    }

    public String getGroupPath() {
        return groupPath;
    }
}
