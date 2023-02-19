package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;

import java.util.Objects;

/**
 * A wrapper for GcGetGroups. When the uhIdentifier of a UH affiliate is passed, GetGroupsCommand on execute
 * fetches(from grouper) results containing the groups that the affiliate is listed in.
 */
public class GetGroupsCommand extends GrouperCommand implements Command<GetGroupsResults> {
    private final GcGetGroups gcGetGroups;

    public GetGroupsCommand(String uhIdentifier, String query) {
        Objects.requireNonNull(uhIdentifier, "uhIdentifier should not be null");
        Objects.requireNonNull(query, "query should not be null");
        WsStemLookup stemLookup = new WsStemLookup(query, null);
        StemScope stemScope = StemScope.ALL_IN_SUBTREE;
        this.gcGetGroups = new GcGetGroups()
                .assignWsStemLookup(stemLookup)
                .assignStemScope(stemScope);
        addUhIdentifier(uhIdentifier);
    }

    public GetGroupsCommand(String uhIdentifier) {
        this(uhIdentifier, "");
    }

    @Override
    public GetGroupsResults execute() {
        WsGetGroupsResults wsGetGroupsResults = this.gcGetGroups.execute();
        return new GetGroupsResults(wsGetGroupsResults);
    }

    private GetGroupsCommand addUhIdentifier(String uhIdentifier) {
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

}
