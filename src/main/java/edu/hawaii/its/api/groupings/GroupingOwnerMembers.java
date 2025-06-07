package edu.hawaii.its.api.groupings;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * When getMembers is called, GroupingOwnerMembers holds the information about UH affiliates that are listed
 * in the owners group.
 */
public class GroupingOwnerMembers implements GroupingResult {
    private String resultCode;
    private String groupPath;
    private GroupingGroupMembers owners;
    private Integer ownerLimit;

    public GroupingOwnerMembers(GetMembersResult getMembersResult, Integer ownerLimit) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setOwners(getMembersResult);
        setOwnerLimit(ownerLimit);
    }

    public GroupingOwnerMembers(SubjectsResults subjectsResults, Integer ownerLimit) {
        setResultCode(subjectsResults.getResultCode());
        setGroupPath(subjectsResults.getGroup().getGroupPath());
        setOwners(subjectsResults);
        setOwnerLimit(ownerLimit);
    }

    public GroupingOwnerMembers(Integer ownerLimit) {
        setResultCode("");
        setGroupPath("");
        this.owners = new GroupingGroupMembers();
        setOwnerLimit(ownerLimit);
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

    @JsonProperty
    public void setOwners(GetMembersResult getMembersResult) {
        this.owners = new GroupingGroupMembers(getMembersResult);
    }

    public void setOwners(SubjectsResults subjectsResults) {
        this.owners = new GroupingGroupMembers(subjectsResults);
    }

    public GroupingGroupMembers getOwners() {
        return this.owners;
    }

    public Integer getOwnerLimit() {
        return this.ownerLimit;
    }

    public void setOwnerLimit(Integer ownerLimit) {
        this.ownerLimit = ownerLimit;
    }
}
