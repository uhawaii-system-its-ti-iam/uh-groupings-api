package edu.hawaii.its.api.groupings;

import java.util.Objects;

import edu.hawaii.its.api.wrapper.AssignAttributesResults;

public class GroupingUpdatedAttributeResult implements GroupingResult {
    private final AssignAttributesResults assignAttributesResults;
    private String name;
    private boolean updatedStatus;
    private boolean currentStatus;

    public GroupingUpdatedAttributeResult(AssignAttributesResults assignAttributesResults) {
        this.assignAttributesResults =
                Objects.requireNonNullElseGet(assignAttributesResults, AssignAttributesResults::new);
        setName();
        setUpdatedStatus();
        setCurrentStatus();
    }

    public GroupingUpdatedAttributeResult() {
        this.assignAttributesResults = new AssignAttributesResults();
        this.updatedStatus = false;
        this.currentStatus = false;
    }

    public String getName() {
        return this.name;
    }

    public boolean getUpdatedStatus() {
        return updatedStatus;
    }

    public boolean getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public String getResultCode() {
            return assignAttributesResults.getResultCode();
    }

    @Override
    public String getGroupPath() {
        return assignAttributesResults.getGroup().getGroupPath();
    }

    private void setName() {
        if (!assignAttributesResults.getAttributesResults().isEmpty()) {
            this.name = assignAttributesResults.getAttributesResults().get(0).getName();
        }
    }

    private void setUpdatedStatus() {
        if (!assignAttributesResults.getAssignAttributeResults().isEmpty()) {
            this.updatedStatus = !assignAttributesResults.getAssignAttributeResults().get(0).isAttributeRemoved();
        }
    }

    private void setCurrentStatus() {
        if (!assignAttributesResults.getAssignAttributeResults().isEmpty()) {
            this.currentStatus = assignAttributesResults.getAssignAttributeResults().get(0).isAttributeChanged() != this.updatedStatus;
        }
    }
}