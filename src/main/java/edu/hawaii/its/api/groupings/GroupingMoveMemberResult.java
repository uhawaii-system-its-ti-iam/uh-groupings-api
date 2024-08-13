package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

/**
 * GroupingsMoveMemberResult holds the result data on when a listed group member is moved from include to exclude
 * visa-versa.
 */
public class GroupingMoveMemberResult implements GroupingResult {
    private final GroupingAddResult addResult;
    private final GroupingRemoveResult removeResult;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    private String resultCode;
    private String groupPath;

    public GroupingMoveMemberResult(AddMemberResult addMemberResult, RemoveMemberResult removeMemberResult) {
        addResult = new GroupingAddResult(addMemberResult);
        removeResult = new GroupingRemoveResult(removeMemberResult);
        setGroupPath(addMemberResult.getGroupPath());
        setResultCode();
    }

    public GroupingMoveMemberResult() {
        addResult = new GroupingAddResult();
        removeResult = new GroupingRemoveResult();
        setGroupPath("");
        setResultCode();
    }

    public GroupingMoveMemberResult(String groupPath, String resultCode) {
        addResult = new GroupingAddResult();
        removeResult = new GroupingRemoveResult();
        setGroupPath(groupPath);
        this.resultCode = resultCode;
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

    public GroupingAddResult getAddResult() {
        return addResult;
    }

    public GroupingRemoveResult getRemoveResult() {
        return removeResult;
    }
}
