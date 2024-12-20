package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.type.OptType;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

public class GroupAttributeResults extends Results {
    private final WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults;

    public GroupAttributeResults(WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults) {
        if (wsGetAttributeAssignmentsResults == null) {
            this.wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
        } else {
            this.wsGetAttributeAssignmentsResults = wsGetAttributeAssignmentsResults;
        }
    }

    public GroupAttributeResults() {
        this.wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
    }

    @Override
    public String getResultCode() {
        return this.wsGetAttributeAssignmentsResults.getResultMetadata().getResultCode();
    }

    public List<GroupAttribute> getGroupAttributes() {
        WsAttributeAssign[] wsAttributeAssigns = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();
        List<GroupAttribute> groupAttributes = new ArrayList<>();
        if (!isEmpty(wsAttributeAssigns)) {
            for (WsAttributeAssign wsAttributeAssign : wsAttributeAssigns) {
                groupAttributes.add(new GroupAttribute(wsAttributeAssign));
            }
        }
        return groupAttributes;
    }

    public List<Group> getGroups() {
        WsGroup[] wsGroups = wsGetAttributeAssignmentsResults.getWsGroups();
        List<Group> groups = new ArrayList<>();
        if (!isEmpty(wsGroups)) {
            for (WsGroup wsGroup : wsGroups) {
                groups.add(new Group(wsGroup));
            }
        }
        return groups;
    }

    public List<AttributesResult> getAttributesResults() {
        WsAttributeDefName[] wsAttributeDefNames = wsGetAttributeAssignmentsResults.getWsAttributeDefNames();
        List<AttributesResult> attributesResults = new ArrayList<>();
        if (!isEmpty(wsAttributeDefNames)) {
            for (WsAttributeDefName wsAttributeDefName : wsAttributeDefNames) {
                attributesResults.add(new AttributesResult(wsAttributeDefName));
            }
        }
        return attributesResults;
    }

    public boolean isOptInOn() {
        return isOptOn(OptType.IN.value());
    }

    public boolean isOptOutOn() {
        return isOptOn(OptType.OUT.value());
    }

    public boolean isOptOn(String optAttributeValue) {
        List<AttributesResult> attributesResults = getAttributesResults();
        for (AttributesResult attributesResult : attributesResults) {
            if (attributesResult.getName().equals(optAttributeValue)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public WsGetAttributeAssignmentsResults getWsGetAttributeAssignmentsResults() {
        return wsGetAttributeAssignmentsResults;
    }

}
