package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

/**
 * A wrapper for GcAssignAttributes. AssignAttributesCommand on execute will update Grouping attributes. The way in which
 * the Grouping attribute is updated is determined by defining the assignment operation with setAssignOperation.
 */
public class AssignAttributesCommand extends GrouperCommand implements Command<AssignAttributesResults> {
    private final GcAssignAttributes gcAssignAttributes;

    public AssignAttributesCommand() {
        this.gcAssignAttributes = new GcAssignAttributes();
    }

    @Override
    public AssignAttributesResults execute() {
        WsAssignAttributesResults wsAssignAttributesResults = this.gcAssignAttributes.execute();
        return new AssignAttributesResults(wsAssignAttributesResults);
    }

    public AssignAttributesCommand setAssignType(String assignType) {
        this.gcAssignAttributes.assignAttributeAssignType(assignType);
        return this;
    }

    public AssignAttributesCommand setAssignOperation(String assignOperation) {
        this.gcAssignAttributes.assignAttributeAssignOperation(assignOperation);
        return this;
    }

    public AssignAttributesCommand addGroupPath(String groupPath) {
        this.gcAssignAttributes.addOwnerGroupName(groupPath);
        return this;
    }

    public AssignAttributesCommand addAttribute(String attribute) {
        this.gcAssignAttributes.addAttributeDefNameName(attribute);
        return this;
    }

    public AssignAttributesCommand setValueOperation(String valueOperation) {
        this.gcAssignAttributes.assignAttributeAssignValueOperation(valueOperation);
        return this;
    }

    public AssignAttributesCommand addValue(String value) {
        this.gcAssignAttributes.addValue(assignAttributeValue(value));
        return this;
    }
}
