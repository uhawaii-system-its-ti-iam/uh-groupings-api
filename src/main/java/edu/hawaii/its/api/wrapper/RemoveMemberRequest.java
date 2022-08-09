package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMemberRequest {

    private final GcDeleteMember gcDeleteMember;

    public RemoveMemberRequest() {
        gcDeleteMember = new GcDeleteMember();
    }

    public RemoveMemberRequest(String groupPath, String uhIdentifier) {
        this();
        assignGroupPath(groupPath);
        addUhIdentifier(uhIdentifier);
    }

    public RemoveMemberResponse send() {
        RemoveMemberResponse removeMemberResponse;
        try {
            WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
            removeMemberResponse = new RemoveMemberResponse(wsDeleteMemberResults);
        } catch (RuntimeException e) {
            throw new RemoveMemberRequestRejectedException(e);
        }
        return removeMemberResponse;
    }

    private RemoveMemberRequest addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private RemoveMemberRequest addUhUuid(String uhUuid) {
        gcDeleteMember.addSubjectId(uhUuid);
        return this;
    }

    private RemoveMemberRequest addUid(String uid) {
        gcDeleteMember.addSubjectIdentifier(uid);
        return this;
    }

    private RemoveMemberRequest assignGroupPath(String groupPath) {
        gcDeleteMember.assignGroupName(groupPath);
        return this;
    }

    private RemoveMemberRequest includeUhMemberDetails(boolean includeDetails) {
        gcDeleteMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

    private boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }
}
