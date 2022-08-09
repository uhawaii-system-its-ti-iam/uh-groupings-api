package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

public class HasMemberResponse {
    private final WsHasMemberResults wsHasMemberResults;
    private static final String IS_MEMBER = "IS_MEMBER";

    public HasMemberResponse(WsHasMemberResults wsHasMemberResults) {
        if (wsHasMemberResults == null) {
            this.wsHasMemberResults = new WsHasMemberResults();
        } else {
            this.wsHasMemberResults = wsHasMemberResults;
        }
    }

    public boolean isMember() {
        return resultCode().equals(IS_MEMBER);
    }

    public String resultCode() {
        if (!hasResults()) {
            return "";
        }
        return wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode();
    }

    private boolean hasResults() {
        if (isEmpty(wsHasMemberResults.getResults())) {
            return false;
        }
        return wsHasMemberResults.getResults()[0] != null;
    }

    private boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }
}
