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

    public AttributesResult() {
        this.wsAttributeDefName = new WsAttributeDefName();
    }

    public String getName() {
        String name = this.wsAttributeDefName.getName();
        return name != null ? name : "";
    }

    public String getDescription() {
        String description = this.wsAttributeDefName.getDescription();
        return description != null ? description : "";
    }


    /* for announcements - need to add to wsAttributeDefName for these properties: message, start, end */
//    public String getMessage() {
//        String message = this.wsAttributeDefName.getMessage();
//        return message != null ? message : "";
//    }



    public String getDefinition() {
        String definition = this.wsAttributeDefName.getAttributeDefName();
        return definition != null ? definition : "";
    }
}
