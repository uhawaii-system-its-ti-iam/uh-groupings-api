package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * When getMembers is called, GroupingOwnerMembers holds the information about UH affiliates that are listed
 * in the owners group.
 */
public class GroupingOwnerMembers implements GroupingResult {
    private String resultCode;
    private String groupPath;

    private GroupingGroupMembers immediateOwners;

    public GroupingOwnerMembers(GetMembersResult getMembersResult) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setImmediateOwners(getMembersResult);
    }

    public GroupingOwnerMembers(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setGroupPath(subjectsResults.getGroup().getGroupPath());
        setImmediateOwners(subjectsResults);
    }

    public GroupingOwnerMembers() {
        setResultCode("");
        setGroupPath("");
        this.immediateOwners = new GroupingGroupMembers();
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

    public void setImmediateOwners(GetMembersResult getMembersResult) {
        this.immediateOwners = new GroupingGroupMembers(getMembersResult);
    }

    public void setImmediateOwners(SubjectsResults subjectsResults) {
        this.immediateOwners = new GroupingGroupMembers(subjectsResults);
    }

    public GroupingGroupMembers getImmediateOwners() {
        return this.immediateOwners;
    }
}
