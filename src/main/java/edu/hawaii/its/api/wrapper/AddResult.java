package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;

public class AddResult extends Results {

    private final WsAddMemberResult wsAddMemberResult;
    private Subject subject;

    public AddResult(WsAddMemberResult wsAddMemberResult) {
        if (wsAddMemberResult == null) {
            this.wsAddMemberResult = new WsAddMemberResult();
        } else {
            this.wsAddMemberResult = wsAddMemberResult;
            if (wsAddMemberResult.getWsSubject() != null) {
                subject = new Subject(wsAddMemberResult.getWsSubject());
            }
        }
    }

    public AddResult() {
        this.wsAddMemberResult = new WsAddMemberResult();
    }

    public String getUhUuid() {
        return subject.getUhUuid();
    }

    public String getUid() {
        String uid = wsAddMemberResult.getWsSubject().getAttributeValue(0);
        return uid != null ? uid : "";
    }

    public String getName() {
        return subject.getName();
    }

    @Override
    public String getResultCode() {
        if (wsAddMemberResult.getResultMetadata() == null) {
            return "";
        }
        String resultCode = wsAddMemberResult.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "";
    }
}
