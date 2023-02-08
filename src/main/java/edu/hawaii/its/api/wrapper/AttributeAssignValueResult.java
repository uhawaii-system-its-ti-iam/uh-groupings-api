package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

public class AttributeAssignValueResult {

    private final WsAttributeAssignValueResult wsAttributeAssignValueResult;

    public AttributeAssignValueResult(WsAttributeAssignValueResult wsAttributeAssignValueResult) {
        if (wsAttributeAssignValueResult == null) {
            this.wsAttributeAssignValueResult = new WsAttributeAssignValueResult();
        } else {
            this.wsAttributeAssignValueResult = wsAttributeAssignValueResult;
        }
    }

    public AttributeAssignValueResult() {
        this.wsAttributeAssignValueResult = new WsAttributeAssignValueResult();
    }

    public boolean isValueChanged() {
        if (this.wsAttributeAssignValueResult.getChanged() == null) {
            return false;
        }
        return this.wsAttributeAssignValueResult.getChanged().equals("T");
    }

    public boolean isValueRemoved() {
        if (this.wsAttributeAssignValueResult.getDeleted() == null) {
            return false;
        }
        return this.wsAttributeAssignValueResult.getDeleted().equals("T");
    }

    public String getValue() {
        WsAttributeAssignValue wsAttributeAssignValue = this.wsAttributeAssignValueResult.getWsAttributeAssignValue();
        if (wsAttributeAssignValue == null) {
            return "";
        }
        String value = wsAttributeAssignValue.getValueSystem();
        return (value != null) ? value : "";
    }
}
