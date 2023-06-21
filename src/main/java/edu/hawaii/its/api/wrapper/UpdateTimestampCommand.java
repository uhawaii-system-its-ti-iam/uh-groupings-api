package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.api.GcAssignAttributes;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;

import java.time.LocalDateTime;
import java.util.Objects;

public class UpdateTimestampCommand extends GrouperCommand implements Command<UpdatedTimestampResult> {

    private static final String ASSIGN_TYPE_GROUP = "group";
    private static final String OPERATION_ASSIGN_ATTRIBUTE = "assign_attr";
    private static final String YYYYMMDDTHHMM = "uh-settings:attributes:for-groups:last-modified:yyyymmddThhmm";
    private static final String OPERATION_REPLACE_VALUES = "replace_values";
    private static final String DATE_FORMAT = "yyyyMMdd'T'HHmm";
    protected final GcAssignAttributes gcAssignAttributes;

    public UpdateTimestampCommand(String groupPath) {
        Objects.requireNonNull(groupPath, "groupPath cannot be null");
        this.gcAssignAttributes = new GcAssignAttributes()
                .assignAttributeAssignType(ASSIGN_TYPE_GROUP)
                .assignAttributeAssignOperation(OPERATION_ASSIGN_ATTRIBUTE)
                .addOwnerGroupName(groupPath)
                .addAttributeDefNameName(YYYYMMDDTHHMM)
                .assignAttributeAssignValueOperation(OPERATION_REPLACE_VALUES)
                .addValue(new DateTimeAttributeValue(Dates.formatDate(
                        Dates.truncateDatePlus60Seconds(LocalDateTime.now()), DATE_FORMAT)).getWsAttributeAssignValue());
    }

    public UpdateTimestampCommand() {
        this.gcAssignAttributes = new GcAssignAttributes();
    }

    @Override
    public UpdatedTimestampResult execute() {
        WsAssignAttributesResults wsAssignAttributesResults = this.gcAssignAttributes.execute();
        return new UpdatedTimestampResult(wsAssignAttributesResults);
    }


    private static class DateTimeAttributeValue {
        public final WsAttributeAssignValue wsAttributeAssignValue;

        public DateTimeAttributeValue(String value) {
            Objects.requireNonNull(value, "value cannot be null");
            this.wsAttributeAssignValue = new WsAttributeAssignValue();
            setValue(value);
        }

        public DateTimeAttributeValue setValue(String value) {
            this.wsAttributeAssignValue.setValueSystem(value);
            return this;
        }

        public WsAttributeAssignValue getWsAttributeAssignValue() {
            return this.wsAttributeAssignValue;
        }

        public String getValue() {
            return this.wsAttributeAssignValue.getValueSystem();
        }

    }
}
