package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;

public class GroupSaveResults extends Results {
    private final WsGroupSaveResults wsGroupSaveResults;
    private Group group;

    public GroupSaveResults(WsGroupSaveResults wsGroupSaveResults) {
        if (wsGroupSaveResults == null) {
            this.wsGroupSaveResults = new WsGroupSaveResults();
            group = new Group();
        } else {
            this.wsGroupSaveResults = wsGroupSaveResults;
            setGroup();
        }
    }

    public GroupSaveResults() {
        this.group = new Group();
        this.wsGroupSaveResults = new WsGroupSaveResults();
    }

    @Override public String getResultCode() {
        String fail = "FAILURE";
        WsGroupSaveResult[] wsGroupSaveResults = this.wsGroupSaveResults.getResults();
        if (isEmpty(wsGroupSaveResults)) {
            return fail;
        }
        WsGroupSaveResult wsGroupSaveResult = wsGroupSaveResults[0];
        return wsGroupSaveResult.getResultMetadata().getResultCode();
    }

    private void setGroup() {
        WsGroupSaveResult[] wsGroupSaveResults = this.wsGroupSaveResults.getResults();
        if (isEmpty(wsGroupSaveResults)) {
            this.group = new Group();
            return;
        }
        WsGroupSaveResult wsGroupSaveResult = wsGroupSaveResults[0];
        this.group = new Group(wsGroupSaveResult.getWsGroup());
    }

    public Group getGroup() {
        return group;
    }
}
