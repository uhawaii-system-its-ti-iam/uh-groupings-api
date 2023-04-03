package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for WsGetMembersResults, which is returned from grouper when GcGetMembers.execute(wrapped by GetMembersCommand)
 * is called. WsGetMembersResults contains a list of WsGetMembersResult, for each group path queried am WsGetMembersResult
 * is added to the list of WsGetMembersResults.
 */
public class GetMembersResults extends Results {

    private final WsGetMembersResults wsGetMembersResults;

    public GetMembersResults(WsGetMembersResults wsGetMembersResults) {
        if (wsGetMembersResults == null) {
            this.wsGetMembersResults = new WsGetMembersResults();
        } else {
            this.wsGetMembersResults = wsGetMembersResults;
        }
    }

    public GetMembersResults() {
        this.wsGetMembersResults = new WsGetMembersResults();
    }

    @Override public String getResultCode() {
        String resultCode = wsGetMembersResults.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "FAILURE";
    }

    public List<GetMembersResult> getMembersResults() {
        List<GetMembersResult> getMembersResults = new ArrayList<>();
        WsGetMembersResult[] wsGetMembersResults = this.wsGetMembersResults.getResults();
        if (isEmpty(wsGetMembersResults)) {
            return getMembersResults;
        }
        for (WsGetMembersResult wsGetMembersResult : wsGetMembersResults) {
            getMembersResults.add(new GetMembersResult(wsGetMembersResult));
        }
        return getMembersResults;
    }
}
