package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;

/**
 * A wrapper for GcGetGroups. When the uhIdentifier of a UH affiliate is passed, GetGroupsCommand on execute
 * fetches(from grouper) results containing the groups that the affiliate is listed in.
 */
public class GetGroupsCommand extends GrouperCommand<GetGroupsCommand> implements Command<GetGroupsResults> {
    private final GcGetGroups gcGetGroups;

    public GetGroupsCommand() {
        this.gcGetGroups = new GcGetGroups();
        this.gcGetGroups.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
    }

    @Override
    public GetGroupsResults execute() {
        WsGetGroupsResults wsGetGroupsResults = this.gcGetGroups.execute();
        return new GetGroupsResults(wsGetGroupsResults);
    }

    @Override
    protected GetGroupsCommand self() {
        return this;
    }

    public GetGroupsCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private GetGroupsCommand addUhUuid(String uhUuid) {
        gcGetGroups.addSubjectId(uhUuid);
        return this;
    }

    private GetGroupsCommand addUid(String uid) {
        gcGetGroups.addSubjectIdentifier(uid);
        return this;
    }

    public GetGroupsCommand query(String query) {
        WsStemLookup stemLookup = new WsStemLookup(query, null);
        StemScope stemScope = StemScope.ALL_IN_SUBTREE;
        this.gcGetGroups.assignWsStemLookup(stemLookup);
        this.gcGetGroups.assignStemScope(stemScope);
        return this;
    }

}
