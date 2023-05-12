package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;

/**
 * GroupingRemoveResult shows the results of after a UH affiliate has been removed from group listing.
 */
public class GroupingRemoveResult extends MemberResult implements GroupingResult {
    private String resultCode;
    private String groupPath;

    public GroupingRemoveResult(RemoveMemberResult removeMemberResult) {
        setResultCode(removeMemberResult.getResultCode());
        setGroupPath(removeMemberResult.getGroupPath());
        setUid(removeMemberResult.getUid());
        setUhUuid(removeMemberResult.getUhUuid());
        setName(removeMemberResult.getName());
    }

    public GroupingRemoveResult() {
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
