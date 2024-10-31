package edu.hawaii.its.api.groupings;

public class GroupingUpdateOptAttributeResult implements GroupingResult {
    private final GroupingUpdatedAttributeResult groupingUpdatedAttributeResult;
    private final GroupingPrivilegeResult optInPrivilegeResult;
    private final GroupingPrivilegeResult optOutPrivilegeResult;

    public GroupingUpdateOptAttributeResult(GroupingUpdatedAttributeResult groupingUpdatedAttributeResult,
            GroupingPrivilegeResult optInPrivilegeResult,
            GroupingPrivilegeResult optOutPrivilegeResult) {
        this.groupingUpdatedAttributeResult = groupingUpdatedAttributeResult;
        this.optInPrivilegeResult = optInPrivilegeResult;
        this.optOutPrivilegeResult = optOutPrivilegeResult;
    }

    public GroupingPrivilegeResult getOptInPrivilegeResult() {
        return optInPrivilegeResult;
    }

    public GroupingPrivilegeResult getOptOutPrivilegeResult() {
        return optOutPrivilegeResult;
    }

    @Override
    public String getResultCode() {
        return groupingUpdatedAttributeResult.getResultCode();
    }

    @Override
    public String getGroupPath() {
        return groupingUpdatedAttributeResult.getGroupPath();
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
}
