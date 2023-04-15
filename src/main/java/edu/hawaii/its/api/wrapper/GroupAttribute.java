package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;

/**
 * A wrapper for WsAttributeAssign.
 */
public class GroupAttribute extends Results {
    private final WsAttributeAssign wsAttributeAssign;

    public GroupAttribute(WsAttributeAssign wsAttributeAssign) {
        if (wsAttributeAssign == null) {
            this.wsAttributeAssign = new WsAttributeAssign();
        } else {
            this.wsAttributeAssign = wsAttributeAssign;
        }
    }

    public GroupAttribute() {
        this.wsAttributeAssign = new WsAttributeAssign();
    }

    public String getAttributeName() {
        String attributeName = this.wsAttributeAssign.getAttributeDefNameName();
        return attributeName != null ? attributeName : "";
    }

    public String getGroupPath() {
        String groupPath = this.wsAttributeAssign.getOwnerGroupName();
        return groupPath != null ? groupPath : "";
    }

    public String getAssignType() {
        String assignType = this.wsAttributeAssign.getAttributeAssignType();
        return assignType != null ? assignType : "";
    }

    @Override public String getResultCode() {
        return getAttributeName().equals("") ? "FAILURE" : "SUCCESS";
    }
}
