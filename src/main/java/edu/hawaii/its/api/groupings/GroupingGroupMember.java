package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Subject;

/**
 * When getMembers is called, GroupingGroupMember holds the information about a UH affiliate that is listed in a grouping.
 */
public class GroupingGroupMember extends MemberResult {
    private String resultCode;

    public GroupingGroupMember(Subject subject) {
        setUid(subject.getUid());
        setUhUuid(subject.getUhUuid());
        setName(subject.getName());
        setFirstName(subject.getFirstName());
        setLastName(subject.getLastName());
        setResultCode(subject.getResultCode());
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
