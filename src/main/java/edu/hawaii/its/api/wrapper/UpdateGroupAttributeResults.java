package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class UpdateGroupAttributeResults  extends AssignAttributesResults{
    public UpdateGroupAttributeResults(
            WsAssignAttributesResults wsAssignAttributesResults) {
        super(wsAssignAttributesResults);
    }

    @Override public String getResultCode() {
        return null;
    }
}
