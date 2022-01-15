package edu.hawaii.its.api.type;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

public class UpdateTimestampResult {
    private WsAssignAttributesResults wsAssignAttributesResults;
    private String pathOfUpdate;
    private WsAttributeAssignValueResult[] timestampUpdateArray;

    public UpdateTimestampResult() {

    }

    public UpdateTimestampResult(WsAssignAttributesResults wsAssignAttributesResults) {
        setWsAssignAttributesResults(wsAssignAttributesResults);
        setPathOfUpdate(wsAssignAttributesResults.getWsGroups()[0].getName());
        setTimestampUpdateArray(
                wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults());
    }

    public void setWsAssignAttributesResults(WsAssignAttributesResults wsAssignAttributesResults) {
        this.wsAssignAttributesResults = wsAssignAttributesResults;
    }

    public void setPathOfUpdate(String pathOfUpdate) {
        this.pathOfUpdate = pathOfUpdate != null ? pathOfUpdate : "";
    }

    public void setTimestampUpdateArray(WsAttributeAssignValueResult[] timestampUpdateArray) {
        this.timestampUpdateArray = timestampUpdateArray;
    }

    public WsAssignAttributesResults getWsAssignAttributesResults() {
        return wsAssignAttributesResults;
    }

    public WsAttributeAssignValueResult[] getTimestampUpdateArray() {
        return timestampUpdateArray;
    }

    public String getPathOfUpdate() {
        return pathOfUpdate;
    }
}