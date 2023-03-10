package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.List;

public class AddMembersCommand extends GrouperCommand implements Command<AddMembersResults> {
    protected final GcAddMember gcAddMember;

    public AddMembersCommand() {
        this.gcAddMember = new GcAddMember();
        includeUhMemberDetails(true);
    }

    public AddMembersResults execute() {
        WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
        return new AddMembersResults(wsAddMemberResults);
    }

    public AddMembersCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    public AddMembersCommand addUhIdentifiers(List<String> uhIdentifiers) {
        for (String uhIdentifier : uhIdentifiers) {
            addUhIdentifier(uhIdentifier);
        }
        return this;
    }

    public AddMembersCommand assignGroupPath(String groupPath) {
        gcAddMember.assignGroupName(groupPath);
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

    public AddMembersCommand includeUhMemberDetails(boolean includeDetails) {
        gcAddMember.assignIncludeSubjectDetail(includeDetails);
        return this;
    }

    public AddMembersCommand replaceGroupMembers(boolean replaceGroupMembers) {
        gcAddMember.assignReplaceAllExisting(replaceGroupMembers);
        return this;
    }

}
