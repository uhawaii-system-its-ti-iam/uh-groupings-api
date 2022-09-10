package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;

import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.util.List;

public class RemoveMembersCommand extends GrouperCommand implements Command<RemoveMembersResults> {
    private final GcDeleteMember gcDeleteMember;

    public RemoveMembersCommand(String groupPath, List<String> uhIdentifiers) {
        gcDeleteMember = new GcDeleteMember();
        for (String uhIdentifier : uhIdentifiers) {
            addUhIdentifier(uhIdentifier);
        }
        includeUhMemberDetails(true);
        assignGroupPath(groupPath);

    }

    public RemoveMembersResults execute() {
        RemoveMembersResults removeMembersResults;
        try {
            WsDeleteMemberResults wsDeleteMemberResults = gcDeleteMember.execute();
            removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        } catch (RuntimeException e) {
            throw new AddMemberRequestRejectedException(e);
        }
        return removeMembersResults;
    }

    private RemoveMembersCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
            includeUhMemberDetails(true);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private RemoveMembersCommand addUhUuid(String uhUuid) {
        gcDeleteMember.addSubjectId(uhUuid);
        return this;
    }

    private RemoveMembersCommand addUid(String uid) {
        gcDeleteMember.addSubjectIdentifier(uid);
        return this;
    }

    private RemoveMembersCommand assignGroupPath(String groupPath) {
        gcDeleteMember.assignGroupName(groupPath);
        return this;
    }

    private RemoveMembersCommand includeUhMemberDetails(boolean includeDetails) {
        gcDeleteMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

}
