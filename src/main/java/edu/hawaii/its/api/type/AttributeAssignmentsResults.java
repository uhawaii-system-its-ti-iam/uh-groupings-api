package edu.hawaii.its.api.type;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper for WsAttributeAssignmentsResults.
 */
public class AttributeAssignmentsResults {
    private final static String OPT_IN = "uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-in";
    private final static String OPT_OUT = "uh-settings:attributes:for-groups:uh-grouping:anyone-can:opt-out";
    private final WsGetAttributeAssignmentsResults results;
    private String attributeDefName;
    private boolean optInOn;
    private boolean optOutOn;

    public AttributeAssignmentsResults(WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults) {
        if (wsGetAttributeAssignmentsResults == null) {
            results = new WsGetAttributeAssignmentsResults();
        } else {
            results = wsGetAttributeAssignmentsResults;
        }
        setAttributeDefName();
        setOpts();
    }

    /**
     * Get a list of distinct ownerGroupNames from the attributeAssigns which pertain to the attributeDefName defined
     * at construction.
     */
    public List<String> getOwnerGroupNames() {
        WsAttributeAssign[] attributeAssigns = results.getWsAttributeAssigns();
        if (isEmpty(attributeAssigns)) {
            return new ArrayList<>();
        }
        Set<String> names = new HashSet<>();
        for (WsAttributeAssign attributeAssign : attributeAssigns) {
            if (attributeAssign.getAttributeDefNameName() != null &&
                    attributeAssign.getAttributeDefNameName().equals(attributeDefName)) {
                names.add(attributeAssign.getOwnerGroupName());
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * True if param attributeDefName matches 'any' attributeDefNameName fields in the WsAttributeAssign array.
     */
    public boolean isAttributeDefName(String attributeDefName) {
        WsAttributeAssign[] attributeAssigns = results.getWsAttributeAssigns();
        if (isEmpty(attributeAssigns)) {
            return false;
        }
        for (WsAttributeAssign attributeAssign : attributeAssigns) {
            if (attributeAssign.getAttributeDefNameName() != null &&
                    attributeAssign.getAttributeDefNameName().equals(attributeDefName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a list of group names from the group.
     */
    public List<String> getGroupNames() {
        WsGroup[] groups = results.getWsGroups();
        if (isEmpty(groups)) {
            return new ArrayList<>();
        }
        return Arrays.stream(groups).map(WsGroup::getName).collect(Collectors.toList());
    }

    public boolean isOptInOn() {
        return optInOn;
    }

    public boolean isOptOutOn() {
        return optOutOn;
    }

    public String getAttributeDefName() {
        return attributeDefName;
    }

    /**
     * Set attributeDefName if WsAttributeDefName[] is not null or empty.
     */
    private void setAttributeDefName() {
        WsAttributeDefName[] defNames = results.getWsAttributeDefNames();
        if (!isEmpty(defNames)) {
            attributeDefName = defNames[0].getName();
        }
    }

    /**
     * Set optInOn and optOutOn if WsAttributeDefName[] is not null or empty.
     */
    private void setOpts() {
        WsAttributeDefName[] defNames = results.getWsAttributeDefNames();
        if (!isEmpty(defNames)) {
            for (WsAttributeDefName attribute : defNames) {
                optInOn = attribute.getName().equals(OPT_IN);
                optOutOn = attribute.getName().equals(OPT_OUT);
            }
        }
    }
    private boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }
}
