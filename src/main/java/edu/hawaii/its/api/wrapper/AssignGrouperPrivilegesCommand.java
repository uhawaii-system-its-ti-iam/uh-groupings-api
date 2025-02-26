package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;

/**
 * A wrapper for GcAssignGrouperPrivilegesLite. Upon passing a privilege name and group path,
 * AssignGrouperPrivilegesCommand on execute sets a groups privilege to is allowed or not.
 */
public class AssignGrouperPrivilegesCommand extends GrouperCommand<AssignGrouperPrivilegesCommand> implements Command<AssignGrouperPrivilegesResult> {

    private final GcAssignGrouperPrivilegesLite gcAssignGrouperPrivilegesLite;

    public AssignGrouperPrivilegesCommand() {
        this.gcAssignGrouperPrivilegesLite = new GcAssignGrouperPrivilegesLite();
        this.gcAssignGrouperPrivilegesLite.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
        this.gcAssignGrouperPrivilegesLite.assignIncludeSubjectDetail(true);
    }

    @Override
    public AssignGrouperPrivilegesResult execute() {
        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult =
                gcAssignGrouperPrivilegesLite.execute();
        return new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
    }

    @Override
    protected AssignGrouperPrivilegesCommand self() {
        return this;
    }

    public AssignGrouperPrivilegesCommand setGroupPath(String groupPath) {
        this.gcAssignGrouperPrivilegesLite.assignGroupName(groupPath);
        return this;
    }

    public AssignGrouperPrivilegesCommand setPrivilege(String privilegeName) {
        this.gcAssignGrouperPrivilegesLite.assignPrivilegeName(privilegeName);
        return this;
    }

    public AssignGrouperPrivilegesCommand setSubjectLookup(String uhIdentifier) {
        this.gcAssignGrouperPrivilegesLite.assignSubjectLookup(subjectLookup(uhIdentifier));
        return this;
    }

    public AssignGrouperPrivilegesCommand setIsAllowed(boolean isAllowed) {
        this.gcAssignGrouperPrivilegesLite.assignAllowed(isAllowed);
        return this;
    }

    public AssignGrouperPrivilegesCommand owner(String uhIdentifier) {
        this.gcAssignGrouperPrivilegesLite.assignActAsSubject(subjectLookup(uhIdentifier));
        return this;
    }
}
