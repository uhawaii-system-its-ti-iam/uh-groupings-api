package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

public class UpdatedTimestampResults extends Results {
    private WsAssignAttributesResults wsAssignAttributesResults;
    private List<Group> groups;
    private List<AttributeAssignValueResult> previousTimestampResults;
    private List<AttributeAssignValueResult> currentTimestampResults;

    public UpdatedTimestampResults(WsAssignAttributesResults wsAssignAttributesResults) {
        Objects.requireNonNull(wsAssignAttributesResults, "wsAssignAttributeResults should not be null");
        this.wsAssignAttributesResults = wsAssignAttributesResults;
        setGroups();
        setChangedValueResults();
        setUpdatedValueResults();
    }

    public UpdatedTimestampResults() {
        this.wsAssignAttributesResults = new WsAssignAttributesResults();
        groups = Collections.emptyList();
        previousTimestampResults = Collections.emptyList();
        currentTimestampResults = Collections.emptyList();
    }

    @Override
    public String getResultCode() {
        String resultCode = this.wsAssignAttributesResults.getResultMetadata().getResultCode();
        return (resultCode != null) ? resultCode : "";
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    private void setGroups() {
        if (isEmpty(this.wsAssignAttributesResults.getWsGroups())) {
            this.groups = Collections.emptyList();
            return;
        }

        this.groups = new ArrayList<>();
        for (WsGroup wsGroup : this.wsAssignAttributesResults.getWsGroups()) {
            this.groups.add(new Group(wsGroup));
        }
    }

    public List<AttributeAssignValueResult> getPreviousTimestampResults() {
        return previousTimestampResults;
    }

    public List<AttributeAssignValueResult> getCurrentTimestampResults() {
        return currentTimestampResults;
    }

    private void setChangedValueResults() {
        if (!hasValueResults()) {
            previousTimestampResults = Collections.emptyList();
            return;
        }

        previousTimestampResults = new ArrayList<>();
        for (WsAssignAttributeResult wsAttributeAssignResult : this.wsAssignAttributesResults.getWsAttributeAssignResults()) {
            this.previousTimestampResults.add(
                    new AttributeAssignValueResult(wsAttributeAssignResult.getWsAttributeAssignValueResults()[0]));
        }
    }

    private void setUpdatedValueResults() {
        if (!hasValueResults()) {
            currentTimestampResults = Collections.emptyList();
            return;
        }

        currentTimestampResults = new ArrayList<>();
        for (WsAssignAttributeResult wsAttributeAssignResult : this.wsAssignAttributesResults.getWsAttributeAssignResults()) {
            WsAttributeAssignValueResult[] results = wsAttributeAssignResult.getWsAttributeAssignValueResults();
            if (results.length < 2) {
                this.currentTimestampResults.add(new AttributeAssignValueResult(results[0]));
            } else {
                this.currentTimestampResults.add(new AttributeAssignValueResult(results[1]));
            }
        }
    }

    public List<Boolean> isTimeUpdatedList() {
        if (!hasValueResults()) {
            return Collections.emptyList();
        }
        return Arrays.stream(this.wsAssignAttributesResults.getWsAttributeAssignResults())
                .map(e -> e.getWsAttributeAssignValueResults().length > 1)
                .collect(Collectors.toList());
    }

    private boolean hasValueResults() {
        if (isEmpty(this.wsAssignAttributesResults.getWsAttributeAssignResults())) {
            return false;
        }
        return !isEmpty(
                this.wsAssignAttributesResults.getWsAttributeAssignResults()[0].getWsAttributeAssignValueResults());
    }
}
