package edu.hawaii.its.api.wrapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;

public class UpdateTimestampCommand extends GrouperCommand<UpdateTimestampCommand> implements Command<UpdatedTimestampResults> {

    private static final String ASSIGN_TYPE_GROUP = "group";
    private static final String OPERATION_ASSIGN_ATTRIBUTE = "assign_attr";
    private static final String YYYYMMDDTHHMM = "uh-settings:attributes:for-groups:last-modified:yyyymmddThhmm";
    private static final String OPERATION_REPLACE_VALUES = "replace_values";
    private static final String DATE_FORMAT = "yyyyMMdd'T'HHmm";
    protected final GcAssignAttributes gcAssignAttributes;

    public UpdateTimestampCommand() {
        this.gcAssignAttributes = new GcAssignAttributes()
                .assignContentType("text/x-json") // Remove after upgrading to Grouper 4
                .assignAttributeAssignType(ASSIGN_TYPE_GROUP)
                .assignAttributeAssignOperation(OPERATION_ASSIGN_ATTRIBUTE)
                .addAttributeDefNameName(YYYYMMDDTHHMM)
                .assignAttributeAssignValueOperation(OPERATION_REPLACE_VALUES)
                .addValue(new DateTimeAttributeValue(Dates.formatDate(
                        Dates.truncateDatePlus60Seconds(LocalDateTime.now()),
                        DATE_FORMAT)).getWsAttributeAssignValue());
    }

    public UpdateTimestampCommand addGroupPath(String groupPath) {
        this.gcAssignAttributes.addOwnerGroupName(groupPath);
        return this;
    }

    public UpdateTimestampCommand addGroupPaths(List<String> groupPaths) {
        for (String groupPath : groupPaths) {
            this.gcAssignAttributes.addOwnerGroupName(groupPath);
        }
        return this;
    }

    @Override
    public UpdatedTimestampResults execute() {
        WsAssignAttributesResults wsAssignAttributesResults = this.gcAssignAttributes.execute();
        return new UpdatedTimestampResults(wsAssignAttributesResults);
    }

    @Override
    protected UpdateTimestampCommand self() {
        return this;
    }

    private static class DateTimeAttributeValue {
        public final WsAttributeAssignValue wsAttributeAssignValue;

        public DateTimeAttributeValue(String value) {
            Objects.requireNonNull(value, "value cannot be null");
            this.wsAttributeAssignValue = new WsAttributeAssignValue();
            setValue(value);
        }

        public WsAttributeAssignValue getWsAttributeAssignValue() {
            return this.wsAttributeAssignValue;
        }

        public String getValue() {
            return this.wsAttributeAssignValue.getValueSystem();
        }

        public DateTimeAttributeValue setValue(String value) {
            this.wsAttributeAssignValue.setValueSystem(value);
            return this;
        }

    }
}
