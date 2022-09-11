package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMemberResult extends Results {
    private final WsDeleteMemberResults wsDeleteMemberResults;
    private final RemoveResult removeResult;

    public RemoveMemberResult(WsDeleteMemberResults wsDeleteMemberResults) {
        if (wsDeleteMemberResults == null) {
            this.wsDeleteMemberResults = new WsDeleteMemberResults();
            removeResult = new RemoveResult();
        } else {
            this.wsDeleteMemberResults = wsDeleteMemberResults;
            if (hasResults()) {
                removeResult = new RemoveResult(wsDeleteMemberResults.getResults()[0]);
            } else {
                removeResult = new RemoveResult();
            }
        }
    }

    public String getGroupPath() {
        String groupPath = null;
        if (wsDeleteMemberResults.getWsGroup() != null) {
            groupPath = wsDeleteMemberResults.getWsGroup().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    @Override
    public String getResultCode() {
        return removeResult.getResultCode();
    }

    public String getUhUuid() {
        return removeResult.getUhUuid();
    }

    public String getUid() {
        return removeResult.getUid();
    }

    public String getName() {
        return removeResult.getName();
    }

    private boolean hasResults() {
        if (isEmpty(wsDeleteMemberResults.getResults())) {
            return false;
        }
        return wsDeleteMemberResults.getResults()[0] != null;
    }
}
