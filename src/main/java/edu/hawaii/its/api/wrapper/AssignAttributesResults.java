package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class AssignAttributesResults extends Results {

    private final WsAssignAttributesResults wsAssignAttributesResults;

    public AssignAttributesResults(WsAssignAttributesResults wsAssignAttributesResults) {
        if (wsAssignAttributesResults == null) {
            this.wsAssignAttributesResults = new WsAssignAttributesResults();
        } else {

            this.wsAssignAttributesResults = wsAssignAttributesResults;
        }
    }


    public String getPathOfUpdate() {
        WsGroup[] wsGroups = wsAssignAttributesResults.getWsGroups();
        if (isEmpty(wsGroups)) {
            return "";
        }
        return new Group(wsGroups[0]).getGroupPath();
    }

    public List<AttributeAssignValueResult> getValueResults() {
        List<AttributeAssignValueResult> attributeAssignValueResults = new ArrayList<>();
        if (isEmpty(wsAssignAttributesResults.getWsAttributeAssignResults()) && isEmpty(
                wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults())) {
            return attributeAssignValueResults;
        }

        WsAttributeAssignValueResult[] wsAttributeAssignValueResults =
                wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults();
        attributeAssignValueResults.add(new AttributeAssignValueResult(wsAttributeAssignValueResults[0]));
        if (wsAttributeAssignValueResults.length == 2) {
            attributeAssignValueResults.add(new AttributeAssignValueResult(wsAttributeAssignValueResults[1]));
        }
        return attributeAssignValueResults;

    }

    /*
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
     */
}
