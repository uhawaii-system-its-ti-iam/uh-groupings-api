package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;

public class HasMemberResult extends MemberResult {

    private final WsHasMemberResult wsHasMemberResult;

    public HasMemberResult(WsHasMemberResult wsHasMemberResult) {
        this.wsHasMemberResult = wsHasMemberResult;
    }

    public HasMemberResult() {
        this.wsHasMemberResult = new WsHasMemberResult();
    }

    public Subject getSubject() {
        if (wsHasMemberResult.getWsSubject() == null) {
            return new Subject();
        }
        return new Subject(wsHasMemberResult.getWsSubject());
    }

    @Override public String getResultCode() {
        String resultCode = wsHasMemberResult.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "";
    }

    @Override public String getUhUuid() {
        return getSubject().getUhUuid();
    }

    @Override public String getName() {
        return getSubject().getName();
    }

    public String getUid() {
        return getSubject().getUid();
    }
}
