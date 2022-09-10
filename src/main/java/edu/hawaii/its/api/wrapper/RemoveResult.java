package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;

public class RemoveResult extends Results {

    private final WsDeleteMemberResult wsDeleteMemberResult;
    private Subject subject;

    public RemoveResult(WsDeleteMemberResult wsDeleteMemberResult) {
        if (wsDeleteMemberResult == null) {
            this.wsDeleteMemberResult = new WsDeleteMemberResult();
        } else {
            this.wsDeleteMemberResult = wsDeleteMemberResult;
            if (wsDeleteMemberResult.getWsSubject() != null) {
                subject = new Subject(wsDeleteMemberResult.getWsSubject());
            }
        }
    }

    public RemoveResult() {
        this.wsDeleteMemberResult = new WsDeleteMemberResult();
    }

    public String getUhUuid() {
        return subject.getUhUuid();
    }

    public String getUid() {
        String uid = wsDeleteMemberResult.getWsSubject().getAttributeValue(0);
        return uid != null ? uid : "";
    }

    public String getName() {
        return subject.getName();
    }

    @Override
    public String getResultCode() {
        if (wsDeleteMemberResult.getResultMetadata() == null) {
            return "";
        }
        String resultCode = wsDeleteMemberResult.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "";
    }
}
