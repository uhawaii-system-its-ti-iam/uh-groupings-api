package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;

import java.util.ArrayList;
import java.util.List;

public class GroupingsGroupsMembers implements GroupingsResult {
    private String resultCode;
    private String groupPath;

    private List<GroupingsGroupMembers> groupsMembersList;

    public GroupingsGroupsMembers(GetMembersResults getMembersResults) {
        setGroupPath("");
        setResultCode(getMembersResults.getResultCode());
        setGroupsMembersList(getMembersResults);
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

    public List<GroupingsGroupMembers> getGroupsMembersList() {
        return groupsMembersList;
    }

    public void setGroupsMembersList(GetMembersResults getMembersResults) {
        this.groupsMembersList = new ArrayList<>();
        for (GetMembersResult getMembersResult : getMembersResults.getMembersResults()) {
            groupsMembersList.add(new GroupingsGroupMembers(getMembersResult));
        }
    }
}
