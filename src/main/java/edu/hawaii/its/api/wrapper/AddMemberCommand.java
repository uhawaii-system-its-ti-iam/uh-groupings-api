package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.Objects;

public class AddMemberCommand extends GrouperCommand implements Command<AddMemberResult> {
    private final GcAddMember gcAddMember;

    public AddMemberCommand(String groupPath, String uhIdentifier) {
        Objects.requireNonNull(uhIdentifier, "uhIdentifier cannot be null");
        Objects.requireNonNull(groupPath, "groupPath cannot be null");
        gcAddMember = new GcAddMember();
        this.includeUhMemberDetails(true)
                .assignGroupPath(groupPath)
                .addUhIdentifier(uhIdentifier);
    }

    public AddMemberResult execute() {
        AddMemberResult addMemberResult;
            WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
            addMemberResult = new AddMemberResult(wsAddMemberResults);
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
