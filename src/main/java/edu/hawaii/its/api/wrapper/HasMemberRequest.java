package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.HasMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

public class HasMemberRequest {

    private GcHasMember gcHasMember;

    public HasMemberRequest() {
        gcHasMember = new GcHasMember();
    }

    public HasMemberRequest(String groupPath, String uhIdentifier) {
        this();
        assignGroupPath(groupPath);
        includeUhMemberDetails(true);
        addUhIdentifier(uhIdentifier);
    }

    public HasMemberResponse send() {
        HasMemberResponse hasMemberResponse;
        try {
            WsHasMemberResults wsHasMemberResults = gcHasMember.execute();
            hasMemberResponse = new HasMemberResponse(wsHasMemberResults);
        } catch (RuntimeException e) {
            throw new HasMemberRequestRejectedException(e);
        }
        return hasMemberResponse;
    }

    private HasMemberRequest addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private HasMemberRequest addUhUuid(String uhUuid) {
        gcHasMember.addSubjectId(uhUuid);
        return this;
    }

    private HasMemberRequest addUid(String uid) {
        gcHasMember.addSubjectIdentifier(uid);
        return this;
    }

    private HasMemberRequest assignGroupPath(String groupPath) {
        gcHasMember.assignGroupName(groupPath);
        return this;
    }

    private HasMemberRequest includeUhMemberDetails(boolean includeDetails) {
        gcHasMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

    private boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }
}
