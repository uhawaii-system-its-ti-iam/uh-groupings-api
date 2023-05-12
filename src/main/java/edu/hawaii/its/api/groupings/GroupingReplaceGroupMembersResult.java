package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AddMembersResults;

/**
 * GroupingReplaceGroupMembersResult shows the results of after an entire groups listing have been replaced. For now
 * this is used for reset include and reset exclude, where the entire group is replaced with an empty list.
 */
public class GroupingReplaceGroupMembersResult extends GroupingAddResults {
    public GroupingReplaceGroupMembersResult(AddMembersResults addMembersResults) {
        super(addMembersResults);
    }

    public GroupingReplaceGroupMembersResult() {
        super(new AddMembersResults());
    }

    @Override
    public String getResultCode() {
        return addMembersResults.getResultCode();
    }
}
