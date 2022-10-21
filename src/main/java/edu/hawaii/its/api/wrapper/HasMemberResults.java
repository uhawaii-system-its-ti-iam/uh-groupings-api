package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

public class HasMemberResults extends Results {
    private WsHasMemberResults wsHasMemberResults;
    private HasMemberResult hasMemberResult;

    public HasMemberResults(WsHasMemberResults wsHasMemberResults) {
        this.wsHasMemberResults = wsHasMemberResults;
        if (this.wsHasMemberResults == null) {
            this.wsHasMemberResults = new WsHasMemberResults();
            this.hasMemberResult = new HasMemberResult();
        } else {
            if (isEmpty(this.wsHasMemberResults.getResults())) {
                this.hasMemberResult = new HasMemberResult();
            } else {
                this.hasMemberResult = new HasMemberResult(wsHasMemberResults.getResults()[0]);
            }
        }
    }

    public HasMemberResults() {
        this.wsHasMemberResults = new WsHasMemberResults();
    }

    public String getGroupPath() {
        WsGroup wsGroup = wsHasMemberResults.getWsGroup();
        return (wsGroup != null && wsGroup.getName() != null) ? wsGroup.getName() : "";
    }

    @Override public String getResultCode() {
        return this.hasMemberResult.getResultCode();
    }

    public String getUhUuid() {
        return this.hasMemberResult.getUhUuid();
    }

    public String getName() {
        return this.hasMemberResult.getName();
    }

    public String getUid() {
        return this.hasMemberResult.getUid();
    }
}
