package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;

public class AddMemberResult extends Results {

    private final WsAddMemberResult wsAddMemberResult;
    private final String groupPath;

    public AddMemberResult(WsAddMemberResult wsAddMemberResult, String groupPath) {
        if (wsAddMemberResult == null) {
            this.wsAddMemberResult = new WsAddMemberResult();
            this.groupPath = "";
        } else {
            this.wsAddMemberResult = wsAddMemberResult;
            this.groupPath = groupPath;
        }
    }

    public AddMemberResult() {
        this.wsAddMemberResult = new WsAddMemberResult();
        this.groupPath = "";
    }

    public String getGroupPath() {
        return groupPath;
    }

    @Override
    public String getResultCode() {
        return wsAddMemberResult.getResultMetadata().getResultCode();
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
        return new Subject(wsAddMemberResult.getWsSubject());
    }
}
