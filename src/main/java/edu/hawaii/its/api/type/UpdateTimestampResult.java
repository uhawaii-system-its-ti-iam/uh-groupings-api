package edu.hawaii.its.api.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

public class UpdateTimestampResult {

    public static final Log logger = LogFactory.getLog(UpdateTimestampResult.class);
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
        logger.info("UpdateTimestampResult; pathOfUpdate: " + getPathOfUpdate() + "; " + getResultLog());
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

    public String getResultLog() {
        WsAttributeAssignValueResult[] wsAttributeAssignValueResults = getTimestampUpdateArray();
        String originalTimestamp = "";
        boolean timeStampUpdated = false;
        String updatedTimeStamp = "";
        String result = "timeStampUpdated: %b; [replacedTimestamp: %s; updatedTimestamp: %s;];";
        if (wsAttributeAssignValueResults[0] != null) {
            originalTimestamp = getValueSystem(wsAttributeAssignValueResults[0]);
        }
        if (wsAttributeAssignValueResults.length == 2 && wsAttributeAssignValueResults[1] != null) {
            updatedTimeStamp = getValueSystem(wsAttributeAssignValueResults[1]);
            timeStampUpdated = !updatedTimeStamp.equals("");
        }
        return String.format(result, timeStampUpdated, originalTimestamp, updatedTimeStamp);
    }

    private String getValueSystem(WsAttributeAssignValueResult wsAttributeAssignValueResult) {
        if (wsAttributeAssignValueResult.getWsAttributeAssignValue() == null) {
            return "";
        }
        String result = wsAttributeAssignValueResult.getWsAttributeAssignValue().getValueSystem();
        return result != null ? result : "";
    }
}
