package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

public class UpdateGroupAttributeCommand extends AssignAttributesCommand implements Command<UpdateGroupAttributeResults>{

    public UpdateGroupAttributeCommand(
            String assignType,
            String assignOperation,
            String defName,
            String groupPath) {
        super();
        super.setAssignType(assignType)
                .setAssignOperation(assignOperation)
                .addAttributeDefName(defName)
                .addGroupPath(groupPath);
    }

    public UpdateGroupAttributeResults execute() {
        WsAssignAttributesResults wsAssignAttributesResults = super.gcAssignAttributes.execute();
        return new UpdateGroupAttributeResults(wsAssignAttributesResults);
    }
}
