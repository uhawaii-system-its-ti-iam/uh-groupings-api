package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveResult;

public class GroupingsRemoveResult extends GroupingsMemberResult {

    public GroupingsRemoveResult(RemoveMemberResult removeMemberResult) {
        resultCode = removeMemberResult.getResultCode();
        uid = removeMemberResult.getUid();
        name = removeMemberResult.getName();
        uhUuid = removeMemberResult.getUhUuid();
    }

    public GroupingsRemoveResult(RemoveResult removeResult) {
        resultCode = removeResult.getResultCode();
        uid = removeResult.getUid();
        name = removeResult.getName();
        uhUuid = removeResult.getUhUuid();
    }
    public GroupingsRemoveResult() {}

    @Override public String getResultCode() {
        return resultCode;
    }

    public String getUid() {
        return uid;
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public String getName() {
        return name;
    }

}
