package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.List;

public class AddMembersCommand extends GrouperCommand implements Command<AddMembersResults> {
    private final GcAddMember gcAddMember;

    public AddMembersCommand(String groupPath, List<String> uhIdentifiers) {
        gcAddMember = new GcAddMember();
        for (String uhIdentifier : uhIdentifiers) {
            addUhIdentifier(uhIdentifier);
        }
        includeUhMemberDetails(true);
        assignGroupPath(groupPath);

    }

    public AddMembersResults execute() {
        AddMembersResults addMembersResults;
        try {
            WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
            addMembersResults = new AddMembersResults(wsAddMemberResults);
        } catch (RuntimeException e) {
            throw new AddMemberRequestRejectedException(e);
        }
        return addMembersResults;
    }

    private AddMembersCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private AddMembersCommand addUhUuid(String uhUuid) {
        gcAddMember.addSubjectId(uhUuid);
        return this;
    }

    private AddMembersCommand addUid(String uid) {
        gcAddMember.addSubjectIdentifier(uid);
        return this;
    }

    private AddMembersCommand assignGroupPath(String groupPath) {
        gcAddMember.assignGroupName(groupPath);
        return this;
    }

    private AddMembersCommand includeUhMemberDetails(boolean includeDetails) {
        gcAddMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

}
