package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

public class AssignAttributesResults extends Results {
    private WsAssignAttributesResults wsAssignAttributesResults;

    public AssignAttributesResults(WsAssignAttributesResults wsAssignAttributesResults) {
        if (wsAssignAttributesResults == null) {
            this.wsAssignAttributesResults = new WsAssignAttributesResults();
        } else {
            this.wsAssignAttributesResults = wsAssignAttributesResults;
        }
    }

    public AssignAttributesResults() {
        this.wsAssignAttributesResults = new WsAssignAttributesResults();
    }

    @Override public String getResultCode() {
        return wsAssignAttributesResults.getResultMetadata().getResultCode();
    }

    public Group getGroup() {
        WsGroup[] wsGroups = wsAssignAttributesResults.getWsGroups();
        if (isEmpty(wsGroups)) {
            return new Group();
        }
        return new Group(wsGroups[0]);
    }

    public List<AttributesResult> getAttributesResults() {
        WsAttributeDefName[] wsAttributeDefNames = this.wsAssignAttributesResults.getWsAttributeDefNames();
        List<AttributesResult> attributesResults = new ArrayList<>();
        if (!isEmpty(wsAttributeDefNames)) {
            for (WsAttributeDefName wsAttributeDefName : wsAttributeDefNames) {
                attributesResults.add(new AttributesResult(wsAttributeDefName));
            }
        }
        return attributesResults;
    }

    public List<AssignAttributeResult> getAssignAttributeResults() {
        WsAssignAttributeResult[] wsAssignAttributeResults =
                this.wsAssignAttributesResults.getWsAttributeAssignResults();
        List<AssignAttributeResult> assignAttributeResults = new ArrayList<>();
        if (!isEmpty(wsAssignAttributeResults)) {
            for (WsAssignAttributeResult wsAssignAttributeResult : wsAssignAttributeResults) {
                assignAttributeResults.add(new AssignAttributeResult(wsAssignAttributeResult));
            }
        }
        return assignAttributeResults;
    }
}
