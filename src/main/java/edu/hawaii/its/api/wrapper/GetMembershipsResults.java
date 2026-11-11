package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMembership;

public class GetMembershipsResults extends Results {

    private final WsGetMembershipsResults wsGetMembershipsResults;

    public GetMembershipsResults(WsGetMembershipsResults wsGetMembershipsResults) {
        this.wsGetMembershipsResults = wsGetMembershipsResults;
    }

    @Override
    public String getResultCode() {
        return wsGetMembershipsResults.getResultMetadata().getResultCode();
    }

    public WsMembership[] getWsMemberships() {
        return wsGetMembershipsResults.getWsMemberships();
    }
}
