package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Subject;

public class GroupingsGroupMember extends MemberResult {
    private String resultCode;

    public GroupingsGroupMember(Subject subject) {
        setUid(subject.getUid());
        setUhUuid(subject.getUhUuid());
        setName(subject.getName());
        setResultCode(subject.getResultCode());
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
