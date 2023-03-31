package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;

public class GroupingsReplaceGroupMembersResult extends GroupingsAddResults {
    public GroupingsReplaceGroupMembersResult(AddMembersResults addMembersResults) {
        super(addMembersResults);
    }

    public GroupingsReplaceGroupMembersResult() {
        super(new AddMembersResults());
    }

    @Override
    public String getResultCode() {
        return addMembersResults.getResultCode();
    }
}
