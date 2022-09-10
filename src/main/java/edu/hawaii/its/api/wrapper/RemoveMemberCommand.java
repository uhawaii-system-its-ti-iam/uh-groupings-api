package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMemberCommand {

    private final GcDeleteMember gcDeleteMember;

    public RemoveMemberCommand() {
        gcDeleteMember = new GcDeleteMember();
    }

    public RemoveMemberCommand(String groupPath, String uhIdentifier) {
        this();
        assignGroupPath(groupPath);
        addUhIdentifier(uhIdentifier);
    }

    public RemoveMemberResult send() {
        RemoveMemberResult removeMemberResult;
        try {
            WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
            removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults);
        } catch (RuntimeException e) {
            throw new RemoveMemberRequestRejectedException(e);
        }
        return removeMemberResult;
    }

    private RemoveMemberCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private RemoveMemberCommand addUhUuid(String uhUuid) {
        gcDeleteMember.addSubjectId(uhUuid);
        return this;
    }

    private RemoveMemberCommand addUid(String uid) {
        gcDeleteMember.addSubjectIdentifier(uid);
        return this;
    }

    private RemoveMemberCommand assignGroupPath(String groupPath) {
        gcDeleteMember.assignGroupName(groupPath);
        return this;
    }

    private RemoveMemberCommand includeUhMemberDetails(boolean includeDetails) {
        gcDeleteMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

    private boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }
}
