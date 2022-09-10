package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class AddMemberResult {

    final static private String SUCCESS = "SUCCESS";

    private final WsAddMemberResults wsAddMemberResults;

    public AddMemberResult(WsAddMemberResults wsAddMemberResults) {
        if (wsAddMemberResults == null) {
            this.wsAddMemberResults = new WsAddMemberResults();
        } else {
            this.wsAddMemberResults = wsAddMemberResults;
        }
    }

    public boolean isSuccess() {
        return resultCode().equals(SUCCESS);
    }

    public String groupPath() {
        String groupPath = null;
        if (wsAddMemberResults.getWsGroupAssigned() != null) {
            groupPath = wsAddMemberResults.getWsGroupAssigned().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    public String resultCode() {
        if (!hasResults()) {
            return "";
        }
        return wsAddMemberResults.getResults()[0].getResultMetadata().getResultCode();
    }

    public String uhUuid() {
        if (!hasResults()) {
            return "";
        }
        String uhUuid = wsAddMemberResults.getResults()[0].getWsSubject().getId();
        return (uhUuid != null) ? uhUuid : "";
    }

    public String uid() {
        if (!hasResults()) {
            return "";
        }
        String uid = wsAddMemberResults.getResults()[0].getWsSubject().getIdentifierLookup();
        if (uid == null && !isEmpty(wsAddMemberResults.getResults()[0].getWsSubject().getAttributeValues())) {
            uid = wsAddMemberResults.getResults()[0].getWsSubject().getAttributeValues()[0];
        }
        return uid != null ? uid : "";
    }

    public String name() {
        if (!hasResults()) {
            return "";
        }
        String name = wsAddMemberResults.getResults()[0].getWsSubject().getName();
        return (name != null) ? name : "";
    }

    private boolean hasResults() {
        if (isEmpty(wsAddMemberResults.getResults())) {
            return false;
        }
        return wsAddMemberResults.getResults()[0] != null;
    }

    private boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }
}
