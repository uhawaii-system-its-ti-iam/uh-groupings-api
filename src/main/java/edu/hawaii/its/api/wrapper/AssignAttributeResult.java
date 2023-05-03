package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;

/**
 * A wrapper for AssignAttributeResult.
 */
public class AssignAttributeResult extends Results {
    private final WsAssignAttributeResult wsAssignAttributeResult;

    public AssignAttributeResult(WsAssignAttributeResult wsAssignAttributeResult) {
        if (wsAssignAttributeResult == null) {
            this.wsAssignAttributeResult = new WsAssignAttributeResult();
        } else {
            this.wsAssignAttributeResult = wsAssignAttributeResult;
        }
    }

    public AssignAttributeResult() {
        this.wsAssignAttributeResult = new WsAssignAttributeResult();
    }

    @Override public String getResultCode() {
        return isEmpty(wsAssignAttributeResult.getWsAttributeAssigns()) ? "FAILURE" : "SUCCESS";
    }

    public boolean isAttributeChanged() {
        return getBoolean(wsAssignAttributeResult.getChanged());
    }

    public boolean isAttributeValuesChanged() {
        return getBoolean(wsAssignAttributeResult.getValuesChanged());
    }

    public boolean isAttributeRemoved() {
        return getBoolean(wsAssignAttributeResult.getDeleted());
    }

}
