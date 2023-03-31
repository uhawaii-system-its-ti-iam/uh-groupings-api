package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;

public class GroupingsRemoveResult extends MemberResult implements GroupingsResult {
    private String resultCode;
    private String groupPath;

    public GroupingsRemoveResult(RemoveMemberResult removeMemberResult) {
        setResultCode(removeMemberResult.getResultCode());
        setGroupPath(removeMemberResult.getGroupPath());
        setUid(removeMemberResult.getUid());
        setUhUuid(removeMemberResult.getUhUuid());
        setName(removeMemberResult.getName());
    }

    public GroupingsRemoveResult() {
        setResultCode("");
        setGroupPath("");
        setUid("");
        setUhUuid("");
        setName("");
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }
}
