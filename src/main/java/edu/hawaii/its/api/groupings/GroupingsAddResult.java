package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddResult;

public class GroupingsAddResult extends GroupingsMemberResult {

    public GroupingsAddResult(AddMemberResult addMemberResult) {
        resultCode = addMemberResult.getResultCode();
        uid = addMemberResult.getUid();
        name = addMemberResult.getName();
        uhUuid = addMemberResult.getUhUuid();
        groupPath = addMemberResult.getGroupPath();
    }

    public GroupingsAddResult(AddResult addResult) {
        resultCode = addResult.getResultCode();
        uid = addResult.getUid();
        name = addResult.getName();
        uhUuid = addResult.getUhUuid();
        groupPath = "";
    }

    public GroupingsAddResult() {
    }

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

    public String getGroupPath() {
        return groupPath;
    }
}
