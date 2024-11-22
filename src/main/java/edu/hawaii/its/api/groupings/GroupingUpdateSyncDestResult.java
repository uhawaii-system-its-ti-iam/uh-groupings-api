package edu.hawaii.its.api.groupings;

public class GroupingUpdateSyncDestResult implements GroupingResult {
    private final GroupingUpdatedAttributeResult groupingUpdatedAttributeResult;

    public GroupingUpdateSyncDestResult() {
        this.groupingUpdatedAttributeResult = new GroupingUpdatedAttributeResult();
    }

    public GroupingUpdateSyncDestResult(GroupingUpdatedAttributeResult groupingUpdatedAttributeResult) {
        this.groupingUpdatedAttributeResult = groupingUpdatedAttributeResult;
    }

    public String getName() {
        return groupingUpdatedAttributeResult.getName();
    }

    public boolean getUpdatedStatus() {
        return groupingUpdatedAttributeResult.getUpdatedStatus();
    }

    public boolean getCurrentStatus() {
        return groupingUpdatedAttributeResult.getCurrentStatus();
    }

    @Override
    public String getResultCode() {
        return groupingUpdatedAttributeResult.getResultCode();
    }

    @Override
    public String getGroupPath() {
        return groupingUpdatedAttributeResult.getGroupPath();
    }
}
