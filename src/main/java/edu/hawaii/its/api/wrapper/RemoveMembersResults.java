package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMembersResults extends Results {

    private final WsDeleteMemberResults wsDeleteMemberResults;

    public RemoveMembersResults(WsDeleteMemberResults wsDeleteMemberResults) {
        if (wsDeleteMemberResults == null) {
            this.wsDeleteMemberResults = new WsDeleteMemberResults();
        } else {
            this.wsDeleteMemberResults = wsDeleteMemberResults;
        }
    }

    public RemoveMembersResults() {
        this.wsDeleteMemberResults = new WsDeleteMemberResults();
    }

    public String getGroupPath() {
        return getGroup().getGroupPath();
    }

    public Group getGroup() {
        return new Group(wsDeleteMemberResults.getWsGroup());
    }

    public List<RemoveMemberResult> getResults() {
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        WsDeleteMemberResult[] wsDeleteMemberResults = this.wsDeleteMemberResults.getResults();
        if (isEmpty(wsDeleteMemberResults)) {
            return removeMemberResults;
        }
        for (WsDeleteMemberResult wsDeleteMemberResult : wsDeleteMemberResults) {
            removeMemberResults.add(new RemoveMemberResult(wsDeleteMemberResult, getGroupPath()));
        }
        return removeMemberResults;
    }

    @Override
    public String getResultCode() {
        return wsDeleteMemberResults.getResultMetadata().getResultCode();
    }
}
