package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

import java.util.Objects;

public class UpdatedTimestampResult extends Results {
    private final WsAssignAttributesResults wsAssignAttributesResults;
    private Group group;
    private AttributeAssignValueResult previousTimestampResult;
    private AttributeAssignValueResult currentTimestampResult;

    public UpdatedTimestampResult(WsAssignAttributesResults wsAssignAttributesResults) {
        Objects.requireNonNull(wsAssignAttributesResults, "wsAssignAttributeResults should not be null");
        this.wsAssignAttributesResults = wsAssignAttributesResults;
        setGroup();
        setChangedValueResult();
        setUpdatedValueResult();

    }

    public UpdatedTimestampResult() {
        this.wsAssignAttributesResults = new WsAssignAttributesResults();
    }

    @Override
    public String getResultCode() {
        String resultCode = this.wsAssignAttributesResults.getResultMetadata().getResultCode();
        return (resultCode != null) ? resultCode : "";
    }

    public Group getGroup() {
        return this.group;
    }

    public AttributeAssignValueResult getPreviousTimestampResult() {
        return previousTimestampResult;
    }

    public AttributeAssignValueResult getCurrentTimestampResult() {
        return currentTimestampResult;
    }

    private void setChangedValueResult() {
        if (!hasValueResults()) {
            this.previousTimestampResult = new AttributeAssignValueResult();
            return;
        }
        WsAttributeAssignValueResult result =
                this.wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults()[0];
        this.previousTimestampResult = new AttributeAssignValueResult(result);
    }

    private void setUpdatedValueResult() {
        if (!hasValueResults()) {
            this.currentTimestampResult = new AttributeAssignValueResult();
            return;
        }
        WsAttributeAssignValueResult[] results =
                this.wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults();
        if (results.length < 2) {
            this.currentTimestampResult = new AttributeAssignValueResult(results[0]);
        } else {
            this.currentTimestampResult = new AttributeAssignValueResult(results[1]);
        }
    }

    public boolean isTimeUpdated() {
        if (!hasValueResults()) {
            return false;
        }
        return this.wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults().length
                > 1;
    }

    private boolean hasValueResults() {
        if (isEmpty(this.wsAssignAttributesResults.getWsAttributeAssignResults())) {
            return false;
        }
        return !isEmpty(
                this.wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults());
    }

    private void setGroup() {
        if (isEmpty(this.wsAssignAttributesResults.getWsGroups())) {
            this.group = new Group();
            return;
        }
        this.group = new Group(this.wsAssignAttributesResults.getWsGroups()[0]);
    }
}
