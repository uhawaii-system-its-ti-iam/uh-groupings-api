package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;

public class RemoveResult extends MemberResult {

    private final WsDeleteMemberResult wsDeleteMemberResult;

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

    public String getUid() {
        String[] attributeValues = wsDeleteMemberResult.getWsSubject().getAttributeValues();
        if (isEmpty(attributeValues)) {
            return "";
        }
        String uid = wsDeleteMemberResult.getWsSubject().getAttributeValue(0);
        return uid != null ? uid : "";
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
