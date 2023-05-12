package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;

/**
 * GroupingAddResult shows the results of after a UH affiliate has been added to a group listing.
 */
public class GroupingAddResult extends MemberResult implements GroupingResult {

    private String resultCode;
    private String groupPath;

    public GroupingAddResult(AddMemberResult addMemberResult) {
        setResultCode(addMemberResult.getResultCode());
        setGroupPath(addMemberResult.getGroupPath());
        setUid(addMemberResult.getUid());
        setUhUuid(addMemberResult.getUhUuid());
        setName(addMemberResult.getName());
    }

    public GroupingAddResult() {
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
