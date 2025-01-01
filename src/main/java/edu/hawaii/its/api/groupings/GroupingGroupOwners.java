package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * When getMembers is called, GroupingGroupOwners holds the information about UH affiliates that are listed in a
 * group such as include, exclude, owners.
 */
public class GroupingGroupOwners implements GroupingResult {
    private String resultCode;
    private String groupPath;

    private GroupingGroupMembers immediateOwners;

    public GroupingGroupOwners(GetMembersResult getMembersResult) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setImmediateOwners(getMembersResult);
    }

    public GroupingGroupOwners(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setGroupPath(subjectsResults.getGroup().getGroupPath());
    }

    public GroupingGroupOwners() {
        setResultCode("");
        setGroupPath("");
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public void setImmediateOwners(GetMembersResult getMembersResult) {
        this.immediateOwners = new GroupingGroupMembers(getMembersResult);
    }

    public GroupingGroupMembers getImmediateOwners() {
        return this.immediateOwners;
    }
}
