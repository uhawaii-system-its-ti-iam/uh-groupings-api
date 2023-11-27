package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;

/**
 * When getMembers is called, GroupingGroupMembers holds the information about UH affiliates that are listed in a
 * group such as include, exclude, owners.
 */
public class GroupingGroupMembers implements GroupingResult {
    private String resultCode;
    private String groupPath;

    private List<GroupingGroupMember> members;

    public GroupingGroupMembers(GetMembersResult getMembersResult) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setMembers(getMembersResult.getSubjects());
    }

    public GroupingGroupMembers() {
        setResultCode("");
        setGroupPath("");
        setMembers(new ArrayList<>());
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

    public void setMembers(List<Subject> subjects) {
        this.members = new ArrayList<>();
        for (Subject subject : subjects) {
            this.members.add(new GroupingGroupMember(subject));
        }
    }

    public List<GroupingGroupMember> getMembers() {
        return this.members;
    }
}
