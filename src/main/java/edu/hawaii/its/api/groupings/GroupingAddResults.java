package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;

/**
 * GroupingAddResults shows the results after multiple UH affiliates have been added to a group listing.
 */
public class GroupingAddResults implements GroupingResult {

    protected AddMembersResults addMembersResults;
    private String groupPath;
    private String resultCode;

    public GroupingAddResults(AddMembersResults addMembersResults) {
        this.addMembersResults = addMembersResults;
        setResultCode();
        setGroupPath(addMembersResults.getGroupPath());
    }

    public GroupingAddResults() {
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

    public List<GroupingAddResult> getResults() {
        List<GroupingAddResult> groupingAddResults = new ArrayList<>();
        for (AddMemberResult addMemberResult : addMembersResults.getResults()) {
            groupingAddResults.add(new GroupingAddResult(addMemberResult));
        }
        return groupingAddResults;
    }

}
