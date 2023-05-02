package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;

public class AssignGrouperPrivilegesResult extends Results {
    private final WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult;

    public AssignGrouperPrivilegesResult(WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult) {
        if (wsAssignGrouperPrivilegesLiteResult == null) {
            this.wsAssignGrouperPrivilegesLiteResult = new WsAssignGrouperPrivilegesLiteResult();
        } else {
            this.wsAssignGrouperPrivilegesLiteResult = wsAssignGrouperPrivilegesLiteResult;
        }
    }

    public AssignGrouperPrivilegesResult() {
        this.wsAssignGrouperPrivilegesLiteResult = new WsAssignGrouperPrivilegesLiteResult();
    }

    @Override public String getResultCode() {
        return this.wsAssignGrouperPrivilegesLiteResult.getResultMetadata().getResultCode();
    }

    public String getPrivilegeName() {
        String privilegeName = this.wsAssignGrouperPrivilegesLiteResult.getPrivilegeName();
        return privilegeName != null ? privilegeName : "";
    }

    public String getPrivilegeType() {
        String privilegeType = this.wsAssignGrouperPrivilegesLiteResult.getPrivilegeType();
        return privilegeType != null ? privilegeType : "";
    }

    public Subject getSubject() {
        return new Subject(this.wsAssignGrouperPrivilegesLiteResult.getWsSubject());
    }

    public Group getGroup() {
        return new Group(this.wsAssignGrouperPrivilegesLiteResult.getWsGroup());
    }

    public boolean isAllowed() {
        return getBoolean(this.wsAssignGrouperPrivilegesLiteResult.getAllowed());
    }

}
