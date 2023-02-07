package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.ArrayList;
import java.util.List;

public class AddMembersResults extends Results {

    protected final WsAddMemberResults wsAddMemberResults;

    public AddMembersResults(WsAddMemberResults wsAddMemberResults) {
        if (wsAddMemberResults == null) {
            this.wsAddMemberResults = new WsAddMemberResults();
        } else {
            this.wsAddMemberResults = wsAddMemberResults;
        }
    }

    public String getGroupPath() {
        String groupPath = null;
        if (wsAddMemberResults.getWsGroupAssigned() != null) {
            groupPath = wsAddMemberResults.getWsGroupAssigned().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    public List<AddResult> getResults() {
        List<AddResult> addResults = new ArrayList<>();
        WsAddMemberResult[] wsResults = wsAddMemberResults.getResults();
        if (isEmpty(wsResults)) {
        } else {
            for (WsAddMemberResult wsResult : wsResults) {
                addResults.add(new AddResult(wsResult));
            }
        }
        return addResults;
    }

    /**
     * If none of the subjects were added, failure, otherwise return success.
     */
    @Override
    public String getResultCode() {
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (AddResult addResult : getResults()) {
            if (addResult.getResultCode().equals(success)) {
                return success;
            }
        }
        return failure;
    }

    public String getResultMessage() {
        return wsAddMemberResults.getResultMetadata().getResultMessage();
    }
}
