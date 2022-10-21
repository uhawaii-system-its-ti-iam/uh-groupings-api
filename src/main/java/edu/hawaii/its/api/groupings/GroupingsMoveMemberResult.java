package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

public class GroupingsMoveMemberResult extends GroupingsMemberResult {
    private final GroupingsAddResult addResult;
    private final GroupingsRemoveResult removeResult;

    public GroupingsMoveMemberResult(AddMemberResult addMemberResult, RemoveMemberResult removeMemberResult) {
        addResult = new GroupingsAddResult(addMemberResult);
        removeResult = new GroupingsRemoveResult(removeMemberResult);
        this.resultCode = addResult.getResultCode();
    }

    @Override public String getResultCode() {
       return this.addResult.getResultCode();
    }

    public GroupingsAddResult getAddResult() {
        return addResult;
    }

    public GroupingsRemoveResult getRemoveResult() {
        return removeResult;
    }
}
