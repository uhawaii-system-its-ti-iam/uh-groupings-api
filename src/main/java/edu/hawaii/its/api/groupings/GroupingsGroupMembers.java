package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;

import java.util.ArrayList;
import java.util.List;

public class GroupingsGroupMembers implements GroupingsResult {
    private String resultCode;
    private String groupPath;

    private List<GroupingsGroupMember> groupMembers;

    public GroupingsGroupMembers(GetMembersResult getMembersResult) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setGroupMembers(getMembersResult.getSubjects());
    }

    public GroupingsGroupMembers() {
        setResultCode("");
        setGroupPath("");
        setGroupMembers(new ArrayList<>());
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

    public void setGroupMembers(List<Subject> subjects) {
        this.groupMembers = new ArrayList<>();
        for (Subject subject : subjects) {
            this.groupMembers.add(new GroupingsGroupMember(subject));
        }
    }

    public List<GroupingsGroupMember> getGroupMembers() {
        return this.groupMembers;
    }
}
