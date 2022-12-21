package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;

public class UsersGroupsResult {
    private final WsGetGroupsResults wsGetGroupsResults;

    public UsersGroupsResult(WsGetGroupsResults wsGetGroupsResults) {
        if (wsGetGroupsResults == null) {
            this.wsGetGroupsResults = new WsGetGroupsResults();
        } else {
            this.wsGetGroupsResults = wsGetGroupsResults;
        }
    }
}
