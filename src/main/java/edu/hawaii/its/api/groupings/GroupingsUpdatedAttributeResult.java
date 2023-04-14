package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AssignAttributesResults;

public class GroupingsUpdatedAttributeResult implements GroupingsResult {
    private final AssignAttributesResults assignAttributesResults;

    public GroupingsUpdatedAttributeResult(AssignAttributesResults assignAttributesResults) {
        if (assignAttributesResults == null) {
            this.assignAttributesResults = new AssignAttributesResults();
        } else {
            this.assignAttributesResults = assignAttributesResults;
        }
    }

    public GroupingsUpdatedAttributeResult() {
        this.assignAttributesResults = new AssignAttributesResults();
    }

    @Override public String getResultCode() {
        return assignAttributesResults.getResultCode();
    }

    @Override public String getGroupPath() {
        return assignAttributesResults.getGroup().getGroupPath();
    }
}
