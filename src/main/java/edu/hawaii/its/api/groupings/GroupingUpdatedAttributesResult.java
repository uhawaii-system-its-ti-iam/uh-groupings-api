package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AssignAttributesResults;

/**
 * GroupingUpdatedAttributesResult contains results of an updated attribute.
 */
public class GroupingUpdatedAttributesResult implements GroupingResult {
    private final AssignAttributesResults assignAttributesResults;

    public GroupingUpdatedAttributesResult(AssignAttributesResults assignAttributesResults) {
        if (assignAttributesResults == null) {
            this.assignAttributesResults = new AssignAttributesResults();
        } else {
            this.assignAttributesResults = assignAttributesResults;
        }
    }

    public GroupingUpdatedAttributesResult() {
        this.assignAttributesResults = new AssignAttributesResults();
    }

    @Override public String getResultCode() {
        return assignAttributesResults.getResultCode();
    }

    @Override public String getGroupPath() {
        return assignAttributesResults.getGroup().getGroupPath();
    }
}
