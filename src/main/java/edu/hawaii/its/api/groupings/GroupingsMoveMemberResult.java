package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

public class GroupingsMoveMemberResult extends GroupingsMemberResult {
    private final GroupingsAddResult addResult;
    private final GroupingsRemoveResult removeResult;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    public GroupingsMoveMemberResult(AddMemberResult addMemberResult, RemoveMemberResult removeMemberResult) {
        addResult = new GroupingsAddResult(addMemberResult);
        removeResult = new GroupingsRemoveResult(removeMemberResult);
        this.resultCode = addResult.getResultCode();
    }

    public GroupingsMoveMemberResult() {
        addResult = new GroupingsAddResult();
        removeResult = new GroupingsRemoveResult();
    }

    @Override
    public String getResultCode() {
        String resultCode = addResult.getResultCode();
        return (resultCode.equals(SUCCESS)) ? resultCode : FAILURE;
    }

    public GroupingsAddResult getAddResult() {
        return addResult;
    }

    public GroupingsRemoveResult getRemoveResult() {
        return removeResult;
    }
}
