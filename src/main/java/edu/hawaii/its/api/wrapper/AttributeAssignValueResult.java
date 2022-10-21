package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

import java.util.Objects;

public class AttributeAssignValueResult extends Results {
    private final WsAttributeAssignValueResult wsAttributeAssignValueResult;

    public AttributeAssignValueResult(WsAttributeAssignValueResult wsAttributeAssignValueResult) {
        if (wsAttributeAssignValueResult == null) {
            this.wsAttributeAssignValueResult = new WsAttributeAssignValueResult();
        } else {
            this.wsAttributeAssignValueResult = wsAttributeAssignValueResult;
        }
    }

    @Override public String getResultCode() {
        return !(getValueSystem().equals("")) ? "SUCCESS" : "FAILURE";
    }

    public boolean isChanged() {
        String result = this.wsAttributeAssignValueResult.getChanged();
        return Objects.equals(result, "T");
    }

    public boolean isDeleted() {
        String result = this.wsAttributeAssignValueResult.getDeleted();
        return Objects.equals(result, "T");
    }

    public String getValueSystem() {
        if (wsAttributeAssignValueResult.getWsAttributeAssignValue() == null) {
            return "";
        }
        String result = wsAttributeAssignValueResult.getWsAttributeAssignValue().getValueSystem();
        return result != null ? result : "";
    }
}
