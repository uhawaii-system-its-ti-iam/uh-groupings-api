package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class AddMemberCommand extends GrouperCommand implements Command<AddMemberResult> {
    private final GcAddMember gcAddMember;

    public AddMemberCommand(String groupPath, String uhIdentifier) {
        gcAddMember = new GcAddMember();
        this.includeUhMemberDetails(true)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier);
    }

    public AddMemberResult execute() {
        AddMemberResult addMemberResult;
        try {
            WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
            addMemberResult = new AddMemberResult(wsAddMemberResults);
        } catch (RuntimeException e) {
            throw new AddMemberRequestRejectedException(e);
        }
        return addMemberResult;
    }

    private AddMemberCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private AddMemberCommand addUhUuid(String uhUuid) {
        gcAddMember.addSubjectId(uhUuid);
        return this;
    }

    private AddMemberCommand addUid(String uid) {
        gcAddMember.addSubjectIdentifier(uid);
        return this;
    }

    private AddMemberCommand assignGroupPath(String groupPath) {
        gcAddMember.assignGroupName(groupPath);
        return this;
    }

    private AddMemberCommand includeUhMemberDetails(boolean includeDetails) {
        gcAddMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }
}
