package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

public class GroupingsMoveMemberResult implements GroupingsResult {
    private final GroupingsAddResult addResult;
    private final GroupingsRemoveResult removeResult;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    private String resultCode;
    private String groupPath;

    public GroupingsMoveMemberResult(AddMemberResult addMemberResult, RemoveMemberResult removeMemberResult) {
        addResult = new GroupingsAddResult(addMemberResult);
        removeResult = new GroupingsRemoveResult(removeMemberResult);
        setGroupPath(addMemberResult.getGroupPath());
        setResultCode();
    }

    public GroupingsMoveMemberResult() {
        addResult = new GroupingsAddResult();
        removeResult = new GroupingsRemoveResult();
        setGroupPath("");
        setResultCode();
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode() {
        String resultCode = addResult.getResultCode();
        this.resultCode = (resultCode.equals(SUCCESS)) ? resultCode : FAILURE;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public GroupingsAddResult getAddResult() {
        return addResult;
    }

    public GroupingsRemoveResult getRemoveResult() {
        return removeResult;
    }
}
