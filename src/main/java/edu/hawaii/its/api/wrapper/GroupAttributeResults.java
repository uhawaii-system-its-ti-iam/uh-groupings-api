package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import java.util.ArrayList;
import java.util.List;

public class GroupAttributeResults extends Results {
    private final WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults;
    private final List<GroupAttribute> groupAttributes;

    public GroupAttributeResults(WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults) {
        groupAttributes = new ArrayList<>();
        if (wsGetAttributeAssignmentsResults == null) {
            this.wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
        } else {
            this.wsGetAttributeAssignmentsResults = wsGetAttributeAssignmentsResults;
            setGroupAttributes();
        }
    }

    @Override
    public String getResultCode() {
        return (groupAttributes.isEmpty()) ? "FAILURE" : "SUCCESS";
    }

    public List<GroupAttribute> getGroupAttributes() {
        return groupAttributes;
    }

    private void setGroupAttributes() {
        WsAttributeAssign[] wsAttributeAssigns = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();
        if (isEmpty(wsAttributeAssigns)) {
            return;
        }

        for (WsAttributeAssign wsAttributeAssign : wsAttributeAssigns) {
            groupAttributes.add(new GroupAttribute(wsAttributeAssign));
        }

    }
}
