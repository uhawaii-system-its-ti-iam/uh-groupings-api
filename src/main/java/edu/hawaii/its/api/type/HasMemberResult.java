package edu.hawaii.its.api.type;

import edu.hawaii.its.api.wrapper.HasMemberResponse;

public class HasMemberResult {
    boolean hasMember;
    String result;

    public HasMemberResult() {
    }

    public HasMemberResult(HasMemberResponse hasMemberResponse) {
        if (hasMemberResponse == null) {
            setHasMember(false);
        } else {
            setHasMember(hasMemberResponse.isMember());
            setResult(hasMemberResponse.resultCode());
        }
    }

    public void setHasMember(boolean hasMember) {
        this.hasMember = hasMember;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isHasMember() {
        return hasMember;
    }

    public String getResult() {
        return result;
    }
}
