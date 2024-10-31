package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.Subject;

public class GroupingPrivilegeResult implements GroupingResult {
    private final AssignGrouperPrivilegesResult assignGrouperPrivilegesResult;

    public GroupingPrivilegeResult(AssignGrouperPrivilegesResult assignGrouperPrivilegesResult) {
        this.assignGrouperPrivilegesResult = assignGrouperPrivilegesResult;
    }

    public Subject getSubject() {
        return assignGrouperPrivilegesResult.getSubject();
    }

    public String getGroupPath() {
        return assignGrouperPrivilegesResult.getGroup().getGroupPath();
    }

    public String getPrivilegeType() {
        return assignGrouperPrivilegesResult.getPrivilegeType();
    }

    public String getPrivilegeName() {
        return assignGrouperPrivilegesResult.getPrivilegeName();
    }

    @Override
    public String getResultCode() {
        return assignGrouperPrivilegesResult.getResultCode();
    }
}
