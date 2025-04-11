package edu.hawaii.its.api.wrapper;

import java.util.List;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * A wrapper for GcAddMember. When a UH identifier and group path are passed, AddMembersCommand on execute adds, to
 * grouper, the UH identifier to the listing of the group at group path. The result fetched contains the attributes of
 * member added and the group added to. Multiple UH identifiers can be added to the query, thus multiple members can be
 * added using one call to grouper. Only one group can be added per query. Passing an invalid identifier, or path
 * will result, on execute, with in a RuntimeException.
 */
public class AddMembersCommand extends GrouperCommand<AddMembersCommand> implements Command<AddMembersResults> {
    protected final GcAddMember gcAddMember;

    public AddMembersCommand() {
        this.gcAddMember = new GcAddMember();
        this.gcAddMember.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
        includeUhMemberDetails(true);
    }

    @Override
    public AddMembersResults execute() {
        WsAddMemberResults wsAddMemberResults = gcAddMember.execute();
        return new AddMembersResults(wsAddMemberResults);
    }

    @Override
    protected AddMembersCommand self() {
        return this;
    }

    public GcAddMember getGcAddMember() {
        return gcAddMember;
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

    public AddMembersCommand addGroupPathOwner(String groupPath) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        // we can check added member is path owner when sourceId is g:gsa. That means it's a group
        wsSubjectLookup.setSubjectSourceId("g:gsa");
        wsSubjectLookup.setSubjectIdentifier(groupPath);
        gcAddMember.addSubjectLookup(wsSubjectLookup);
        return this;
    }

    public AddMembersCommand addGroupPathOwners(List<String> groupPaths) {
        for(String groupPath: groupPaths){
            addGroupPathOwner(groupPath);
        }
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

    public AddMembersCommand owner(String uhIdentifier) {
        gcAddMember.assignActAsSubject(subjectLookup(uhIdentifier));
        return this;
    }

}
