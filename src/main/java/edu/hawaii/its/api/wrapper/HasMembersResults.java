package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

public class HasMembersResults extends Results {

    private final WsHasMemberResults wsHasMemberResults;

    public HasMembersResults(WsHasMemberResults wsHasMemberResults) {
        this.wsHasMemberResults = wsHasMemberResults;
    }

    public HasMembersResults() {
        this.wsHasMemberResults = new WsHasMemberResults();
    }

    public List<HasMemberResult> getResults() {
        WsHasMemberResult[] wsResults = wsHasMemberResults.getResults();
        if (isEmpty(wsResults)) {
            return new ArrayList<>();
        }
        List<HasMemberResult> hasMembersResults = new ArrayList<>();
        for (WsHasMemberResult wsResult : wsResults) {
            hasMembersResults.add(new HasMemberResult(wsResult));
        }
        return hasMembersResults;
    }

    @Override public String getResultCode() {
        return wsHasMemberResults.getResultMetadata().getResultCode();
    }

    public String getGroupPath() {
        return getGroup().getGroupPath();
    }

    public Group getGroup() {
        WsGroup wsGroup = wsHasMemberResults.getWsGroup();
        if (wsGroup == null) {
            return new Group();
        }
        return new Group(wsGroup);
    }

    @JsonIgnore
    public WsHasMemberResults getWsHasMemberResults() {
        return wsHasMemberResults;
    }
}
