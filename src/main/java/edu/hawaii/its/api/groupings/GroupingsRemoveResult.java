package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveResult;

public class GroupingsRemoveResult extends GroupingsMemberResult {

    public GroupingsRemoveResult(RemoveMemberResult removeMemberResult) {
        resultCode = removeMemberResult.getResultCode();
        uid = removeMemberResult.getUid();
        name = removeMemberResult.getName();
        uhUuid = removeMemberResult.getUhUuid();
        groupPath = removeMemberResult.getGroupPath();
    }

    public GroupingsRemoveResult(RemoveResult removeResult) {
        resultCode = removeResult.getResultCode();
        uid = removeResult.getUid();
        name = removeResult.getName();
        uhUuid = removeResult.getUhUuid();
        groupPath = "";
    }

    public GroupingsRemoveResult() {
    }

    @Override public String getResultCode() {
        return (resultCode != null) ? resultCode : "";
    }

    public String getUid() {
        return (uid != null) ? uid : "";
    }

    public String getUhUuid() {
        return (uhUuid != null) ? uhUuid : "";
    }

    public String getName() {
        return (name != null) ? name : "";
    }

    public String getGroupPath() {
        return (groupPath != null) ? groupPath : "";
    }

}
