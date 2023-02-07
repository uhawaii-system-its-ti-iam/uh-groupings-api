package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.ReplaceGroupMembersResult;

public class GroupingsReplaceGroupMembersResult extends GroupingsAddResults {
    public GroupingsReplaceGroupMembersResult(AddMembersResults addMembersResults) {
        super(addMembersResults);
    }

    public GroupingsReplaceGroupMembersResult() {
        super(new ReplaceGroupMembersResult());
    }

    @Override
    public String getResultCode() {
        return addMembersResults.getResultCode();
    }
}
