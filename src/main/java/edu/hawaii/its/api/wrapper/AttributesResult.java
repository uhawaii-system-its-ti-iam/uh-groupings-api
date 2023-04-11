package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;

public class AttributesResult {
    private final WsAttributeDefName wsAttributeDefName;

    public AttributesResult(WsAttributeDefName wsAttributeDefName) {
        if (wsAttributeDefName == null) {
            this.wsAttributeDefName = new WsAttributeDefName();
        } else {
            this.wsAttributeDefName = wsAttributeDefName;
        }
    }

    public String getName() {
        String name = this.wsAttributeDefName.getName();
        return name != null ? name : "";
    }

    public String getDescription() {
        String description = this.wsAttributeDefName.getDescription();
        return description != null ? description : "";
    }

}
