package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.util.ArrayList;
import java.util.List;

public class RemoveMembersResults extends Results {

    private final WsDeleteMemberResults wsDeleteMemberResults;

    public RemoveMembersResults(WsDeleteMemberResults wsDeleteMemberResults) {
        if (wsDeleteMemberResults == null) {
            this.wsDeleteMemberResults = new WsDeleteMemberResults();
        } else {
            this.wsDeleteMemberResults = wsDeleteMemberResults;
        }
    }

    public String getGroupPath() {
        String groupPath = null;
        if (wsDeleteMemberResults.getWsGroup() != null) {
            groupPath = wsDeleteMemberResults.getWsGroup().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    public List<RemoveResult> getResults() {
        List<RemoveResult> addResults = new ArrayList<>();
        WsDeleteMemberResult[] wsResults = wsDeleteMemberResults.getResults();
        if (isEmpty(wsResults)) {
        } else {
            for (WsDeleteMemberResult wsResult : wsResults) {
                addResults.add(new RemoveResult(wsResult));
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
        for (RemoveResult removeResult : getResults()) {
            if (removeResult.getResultCode().equals(success)) {
                return success;
            }
        }
        return failure;
    }
}
