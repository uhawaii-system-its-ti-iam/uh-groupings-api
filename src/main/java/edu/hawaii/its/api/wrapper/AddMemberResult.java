package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class AddMemberResult extends Results {


    private final WsAddMemberResults wsAddMemberResults;
    private final AddResult addResult;

    public AddMemberResult(WsAddMemberResults wsAddMemberResults) {
        if (wsAddMemberResults == null) {
            this.wsAddMemberResults = new WsAddMemberResults();
            addResult = new AddResult();
        } else {
            this.wsAddMemberResults = wsAddMemberResults;
            if (hasResults()) {
                addResult = new AddResult(wsAddMemberResults.getResults()[0]);
            } else {
                addResult = new AddResult();
            }
        }
    }

    public String getGroupPath() {
        String groupPath = null;
        if (wsAddMemberResults.getWsGroupAssigned() != null) {
            groupPath = wsAddMemberResults.getWsGroupAssigned().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    @Override
    public String getResultCode() {
        return addResult.getResultCode();
    }

    public String getUhUuid() {
        return addResult.getUhUuid();
    }

    public String getUid() {
        return addResult.getUid();
    }

    public String getName() {
        return addResult.getName();
    }

    private boolean hasResults() {
        if (isEmpty(wsAddMemberResults.getResults())) {
            return false;
        }
        return wsAddMemberResults.getResults()[0] != null;
    }
}
