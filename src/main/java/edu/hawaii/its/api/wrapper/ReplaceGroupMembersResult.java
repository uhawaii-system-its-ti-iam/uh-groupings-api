package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

public class ReplaceGroupMembersResult extends AddMembersResults {

    public ReplaceGroupMembersResult(WsAddMemberResults wsAddMemberResults) {
        super(wsAddMemberResults);
    }

    public ReplaceGroupMembersResult() {
        super(new WsAddMemberResults());
    }

    @Override
    public String getResultCode() {
        return wsAddMemberResults.getResultMetadata().getResultCode();
    }
}
