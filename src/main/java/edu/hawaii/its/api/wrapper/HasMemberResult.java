package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;

public class HasMemberResult extends MemberResult {

    private final WsHasMemberResult wsHasMemberResult;

    private final Subject subject;

    public HasMemberResult(WsHasMemberResult wsHasMemberResult) {
        if (wsHasMemberResult == null) {
            this.wsHasMemberResult = new WsHasMemberResult();
            this.subject = new Subject();
        } else {
            this.wsHasMemberResult = wsHasMemberResult;
            if (wsHasMemberResult.getWsSubject() == null) {
                this.subject = new Subject();
            } else {
                this.subject = new Subject(wsHasMemberResult.getWsSubject());
            }

        }
    }

    public HasMemberResult() {
        this.wsHasMemberResult = new WsHasMemberResult();
        this.subject = new Subject();
    }

    @Override public String getResultCode() {
        if (wsHasMemberResult.getResultMetadata() == null) {
            return "";
        }
        String resultCode = wsHasMemberResult.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "";
    }

    @Override public String getUhUuid() {
        return subject.getUhUuid();
    }

    @Override public String getName() {
        return subject.getName();
    }

    public String getUid() {
        String[] attributeValues = wsHasMemberResult.getWsSubject().getAttributeValues();
        if (isEmpty(attributeValues)) {
            return "";
        }
        String uid = wsHasMemberResult.getWsSubject().getAttributeValue(0);
        return uid != null ? uid : "";
    }
}
