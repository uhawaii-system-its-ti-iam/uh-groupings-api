package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

import java.time.LocalDateTime;

public class UpdateTimestampCommand extends AssignAttributesCommand implements Command<UpdateTimestampResults> {
    public UpdateTimestampCommand(
            String assignType,
            String assignOperation,
            String groupPath,
            String defName,
            String valueOperation,
            LocalDateTime dateTime) {
        super();
        super.setAssignType(assignType)
                .setAssignOperation(assignOperation)
                .addGroupPath(groupPath)
                .addAttributeDefName(defName)
                .setValueOperation(valueOperation)
                .addValue(Dates.formatDate(dateTime.plusMinutes(1), "yyyyMMdd'T'HHmm"));
    }

    public UpdateTimestampResults execute() {
        WsAssignAttributesResults wsAssignAttributesResults = super.gcAssignAttributes.execute();
        return new UpdateTimestampResults(wsAssignAttributesResults);
    }
}
