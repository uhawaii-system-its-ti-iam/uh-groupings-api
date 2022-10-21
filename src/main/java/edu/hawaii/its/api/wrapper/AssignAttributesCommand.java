package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;

import java.util.Objects;

public abstract class AssignAttributesCommand {

    protected final GcAssignAttributes gcAssignAttributes;

    public AssignAttributesCommand() {
        this.gcAssignAttributes = new GcAssignAttributes();
    }

    protected AssignAttributesCommand setAssignType(String assignType) {
        Objects.requireNonNull(assignType, "assignType cannot be null");
        this.gcAssignAttributes.assignAttributeAssignType(assignType);
        return this;
    }

    protected AssignAttributesCommand setAssignOperation(String assignOperation) {
        Objects.requireNonNull(assignOperation, "assignOperation cannot be null");
        this.gcAssignAttributes.assignAttributeAssignOperation(assignOperation);
        return this;
    }

    protected AssignAttributesCommand addGroupPath(String groupPath) {
        Objects.requireNonNull(groupPath, "groupPath cannot be null");
        this.gcAssignAttributes.addOwnerGroupName(groupPath);
        return this;
    }

    protected AssignAttributesCommand addAttributeDefName(String defName) {
        Objects.requireNonNull(defName, "defName cannot be null");
        this.gcAssignAttributes.addAttributeDefNameName(defName);
        return this;
    }

    protected AssignAttributesCommand setValueOperation(String valueOperation) {
        Objects.requireNonNull(valueOperation, "valueOperation cannot be null");
        this.gcAssignAttributes.assignAttributeAssignValueOperation(valueOperation);
        return this;
    }

    protected AssignAttributesCommand addValue(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
        wsAttributeAssignValue.setValueSystem(value);
        this.gcAssignAttributes.addValue(wsAttributeAssignValue);
        return this;
    }

}
