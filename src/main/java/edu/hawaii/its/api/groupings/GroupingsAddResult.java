package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;

public class GroupingsAddResult extends MemberResult implements GroupingsResult {

    private String resultCode;
    private String groupPath;

    public GroupingsAddResult(AddMemberResult addMemberResult) {
        setResultCode(addMemberResult.getResultCode());
        setGroupPath(addMemberResult.getGroupPath());
        setUid(addMemberResult.getUid());
        setUhUuid(addMemberResult.getUhUuid());
        setName(addMemberResult.getName());
    }

    public GroupingsAddResult() {
        setResultCode("");
        setGroupPath("");
        setUid("");
        setUhUuid("");
        setName("");
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }
}
