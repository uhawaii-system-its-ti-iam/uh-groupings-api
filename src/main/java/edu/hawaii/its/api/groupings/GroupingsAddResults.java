package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AddResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupingsAddResults extends GroupingsMembersResults implements MemberResults<GroupingsAddResult> {

    protected final List<GroupingsAddResult> groupingsAddResults;
    protected final AddMembersResults addMembersResults;

    public GroupingsAddResults(AddMembersResults addMembersResults) {
        Objects.requireNonNull(addMembersResults, "addMembersResults cannot be null");
        this.addMembersResults = addMembersResults;
        groupingsAddResults = new ArrayList<>();
        setGroupPath(addMembersResults.getGroupPath());
        for (AddResult addResult : addMembersResults.getResults()) {
            groupingsAddResults.add(new GroupingsAddResult(addResult));
        }
    }

    @Override
    public String getResultCode() {
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (GroupingsAddResult groupingsAddResult : groupingsAddResults) {
            if (groupingsAddResult.resultCode.equals(success)) {
                return success;
            }
        }
        return failure;
    }

    @Override
    public List<GroupingsAddResult> getResults() {
        return groupingsAddResults;
    }

}
