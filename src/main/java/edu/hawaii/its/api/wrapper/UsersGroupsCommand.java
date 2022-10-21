package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;

import org.springframework.beans.factory.annotation.Value;

public class UsersGroupsCommand extends GrouperCommand implements Command<UsersGroupsResult> {

    private final GcGetGroups gcGetGroups;

    @Value("${groupings.api.stem}")
    private String STEM;

    public UsersGroupsCommand(String uhIdentifier) {
        gcGetGroups = new GcGetGroups();
        WsStemLookup stemLookup = new WsStemLookup(STEM, null);
        addUhIdentifier(uhIdentifier);
        gcGetGroups.assignWsStemLookup(stemLookup);
        gcGetGroups.assignStemScope(StemScope.ALL_IN_SUBTREE);

    }

    @Override public UsersGroupsResult execute() {
        UsersGroupsResult usersGroupsResult;
        WsGetGroupsResults wsGetGroupsResults = gcGetGroups.execute();
        JsonUtil.printJson(wsGetGroupsResults);
        usersGroupsResult = new UsersGroupsResult(wsGetGroupsResults);
        return usersGroupsResult;
    }

    private UsersGroupsCommand addUhIdentifier(String uhIdentifier) {
        if (isUhUuid(uhIdentifier)) {
            addUhUuid(uhIdentifier);
        } else {
            addUid(uhIdentifier);
        }
        return this;
    }

    private UsersGroupsCommand addUhUuid(String uhIdentifier) {
        gcGetGroups.addSubjectId(uhIdentifier);
        return this;
    }

    private UsersGroupsCommand addUid(String uhIdentifier) {
        gcGetGroups.addSubjectIdentifier(uhIdentifier);
        return this;
    }
}
