package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class AddMemberRequest {
    private final GcAddMember gcAddMember;

    public AddMemberRequest(String groupPath, String memberToAdd) {
        gcAddMember = new GcAddMember();
        addUhIdentifier(memberToAdd);
        assignGroupPath(groupPath);
    }

    public AddMemberRequest() {
        gcAddMember = new GcAddMember();
    }

    public AddMemberResponse send() {
        AddMemberResponse addMemberResponse;
        try {
            WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
            addMemberResponse = new AddMemberResponse(wsAddMemberResults);
        } catch (RuntimeException e) {
            throw new AddMemberRequestRejectedException(e);
        }
        return addMemberResponse;
    }

    private AddMemberRequest addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private AddMemberRequest addUhUuid(String uhUuid) {
        gcAddMember.addSubjectId(uhUuid);
        return this;
    }

    private AddMemberRequest addUid(String uid) {
        gcAddMember.addSubjectIdentifier(uid);
        return this;
    }

    private AddMemberRequest assignGroupPath(String groupPath) {
        gcAddMember.assignGroupName(groupPath);
        return this;
    }

    private AddMemberRequest includeUhMemberDetails(boolean includeDetails) {
        gcAddMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

    private boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }

}
