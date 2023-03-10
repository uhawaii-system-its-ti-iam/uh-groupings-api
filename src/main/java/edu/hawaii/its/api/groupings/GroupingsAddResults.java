package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;

import java.util.ArrayList;
import java.util.List;

public class GroupingsAddResults implements GroupingsResult {

    protected AddMembersResults addMembersResults;

    private String groupPath;
    private String resultCode;

    public GroupingsAddResults(AddMembersResults addMembersResults) {
        this.addMembersResults = addMembersResults;
        setResultCode();
        setGroupPath(addMembersResults.getGroupPath());
    }

    public GroupingsAddResults() {
        addMembersResults = new AddMembersResults();
        setResultCode();
        setGroupPath("");
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode() {
        for (AddMemberResult addMemberResult : addMembersResults.getResults()) {
            if (addMemberResult.getResultCode().equals("SUCCESS")) {
                this.resultCode = "SUCCESS";
                return;
            }
        }
        this.resultCode = "FAILURE";
    }

    @Override
    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public List<GroupingsAddResult> getResults() {
        List<GroupingsAddResult> groupingsAddResults = new ArrayList<>();
        for (AddMemberResult addMemberResult : addMembersResults.getResults()) {
            groupingsAddResults.add(new GroupingsAddResult(addMemberResult));
        }
        return groupingsAddResults;
    }

}
