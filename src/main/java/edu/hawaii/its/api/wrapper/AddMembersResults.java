package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

/**
 * A wrapper for AddMembersResults.
 */
public class AddMembersResults extends Results {

    protected final WsAddMemberResults wsAddMemberResults;

    public AddMembersResults(WsAddMemberResults wsAddMemberResults) {
        if (wsAddMemberResults == null) {
            this.wsAddMemberResults = new WsAddMemberResults();
        } else {
            this.wsAddMemberResults = wsAddMemberResults;
        }
    }

    public AddMembersResults() {
        wsAddMemberResults = new WsAddMemberResults();
    }

    public String getGroupPath() {
        return getGroup().getGroupPath();
    }

    public List<AddMemberResult> getResults() {
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        WsAddMemberResult[] wsAddMemberResults = this.wsAddMemberResults.getResults();
        if (isEmpty(wsAddMemberResults)) {
            return addMemberResults;
        }
        for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults) {
            addMemberResults.add(new AddMemberResult(wsAddMemberResult, getGroupPath()));
        }
        return addMemberResults;
    }

    /**
     * If none of the subjects were added, failure, otherwise return success.
     */
    @Override
    public String getResultCode() {
        return wsAddMemberResults.getResultMetadata().getResultCode();
    }

    public Group getGroup() {
        return new Group(wsAddMemberResults.getWsGroupAssigned());
    }
}
