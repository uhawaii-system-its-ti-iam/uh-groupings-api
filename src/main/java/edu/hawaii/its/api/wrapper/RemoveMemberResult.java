package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMemberResult {
    final static private String SUCCESS = "SUCCESS";
    private final WsDeleteMemberResults wsDeleteMemberResults;

    public RemoveMemberResult(WsDeleteMemberResults wsDeleteMemberResults) {
        if (wsDeleteMemberResults == null) {
            this.wsDeleteMemberResults = new WsDeleteMemberResults();
        } else {
            this.wsDeleteMemberResults = wsDeleteMemberResults;
        }
    }

    public boolean isSuccess() {
        return resultCode().equals(SUCCESS);
    }

    public String groupPath() {
        String groupPath = null;
        if (wsDeleteMemberResults.getWsGroup() != null) {
            groupPath = wsDeleteMemberResults.getWsGroup().getName();
        }
        return (groupPath != null) ? groupPath : "";
    }

    public String resultCode() {
        if (!hasResults()) {
            return "";
        }
        return wsDeleteMemberResults.getResults()[0].getResultMetadata().getResultCode();
    }

    public String getResultCode() {
        return resultCode();
    }

    public String uhUuid() {
        if (!hasResults()) {
            return "";
        }
        String uhUuid = wsDeleteMemberResults.getResults()[0].getWsSubject().getId();
        return (uhUuid != null) ? uhUuid : "";
    }

    public String uid() {
        if (!hasResults()) {
            return "";
        }
        String uid = wsDeleteMemberResults.getResults()[0].getWsSubject().getIdentifierLookup();
        if (uid == null && !isEmpty(wsDeleteMemberResults.getResults()[0].getWsSubject().getAttributeValues())) {
            uid = wsDeleteMemberResults.getResults()[0].getWsSubject().getAttributeValues()[0];
        }
        return uid != null ? uid : "";
    }

    public String name() {
        if (!hasResults()) {
            return "";
        }
        String name = wsDeleteMemberResults.getResults()[0].getWsSubject().getName();
        return (name != null) ? name : "";
    }

    private boolean hasResults() {
        if (isEmpty(wsDeleteMemberResults.getResults())) {
            return false;
        }
        return wsDeleteMemberResults.getResults()[0] != null;
    }

    private boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }

    @Override public String toString() {
        return super.toString();
    }
}
