package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;

public class RemoveMemberResult extends Results {
    private final WsDeleteMemberResult wsDeleteMemberResult;
    private final String groupPath;

    public RemoveMemberResult(WsDeleteMemberResult wsDeleteMemberResult, String groupPath) {
        if (wsDeleteMemberResult == null) {
            this.wsDeleteMemberResult = new WsDeleteMemberResult();
            this.groupPath = "";
        } else {
            this.wsDeleteMemberResult = wsDeleteMemberResult;
            this.groupPath = groupPath;
        }
    }

    public RemoveMemberResult() {
        this.wsDeleteMemberResult = new WsDeleteMemberResult();
        this.groupPath = "";
    }

    public String getGroupPath() {
        return groupPath;
    }

    @Override
    public String getResultCode() {
        return wsDeleteMemberResult.getResultMetadata().getResultCode();
    }

    public String getUhUuid() {
        return getSubject().getUhUuid();
    }

    public String getUid() {
        return getSubject().getUid();
    }

    public String getName() {
        return getSubject().getName();
    }

    public Subject getSubject() {
        return new Subject(wsDeleteMemberResult.getWsSubject());
    }
}
