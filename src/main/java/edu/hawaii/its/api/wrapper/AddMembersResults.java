package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

/**
 * A wrapper for AddMembersResults.
 */
public class AddMembersResults extends Results {

    protected final WsAddMemberResults wsAddMemberResults;

    public AddMembersResults(WsAddMemberResults wsAddMemberResults) {
        if (wsAddMemberResults == null) {
            this.wsAddMemberResults = new WsAddMemberResults();
        } else {
            this.wsAddMemberResults = wsAddMemberResults;
        }
    }

    public AddMembersResults() {
        wsAddMemberResults = new WsAddMemberResults();
    }

    /**
     * Merge the results of an add that was sent to Grouper in batches into one result, in batch order, as if the
     * add had been sent in a single request.
     */
    public static AddMembersResults merge(List<AddMembersResults> batchResults) {
        if (batchResults.isEmpty()) {
            return new AddMembersResults();
        }
        if (batchResults.size() == 1) {
            return batchResults.get(0);
        }
        List<WsAddMemberResults> batches = batchResults.stream().map(results -> results.wsAddMemberResults).toList();
        WsAddMemberResults merged = new WsAddMemberResults();
        merged.setResults(batches.stream()
                .map(WsAddMemberResults::getResults)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toArray(WsAddMemberResult[]::new));
        merged.setWsGroupAssigned(batches.get(0).getWsGroupAssigned());
        merged.setSubjectAttributeNames(batches.get(0).getSubjectAttributeNames());
        merged.setResultMetadata(
                mergedResultMetadata(batches.stream().map(WsAddMemberResults::getResultMetadata).toList()));
        return new AddMembersResults(merged);
    }

    public String getGroupPath() {
        return getGroup().getGroupPath();
    }

    public List<AddMemberResult> getResults() {
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        WsAddMemberResult[] wsAddMemberResults = this.wsAddMemberResults.getResults();
        if (isEmpty(wsAddMemberResults)) {
            return addMemberResults;
        }
        for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults) {
            addMemberResults.add(new AddMemberResult(wsAddMemberResult, getGroupPath()));
        }
        return addMemberResults;
    }

    /**
     * If none of the subjects were added, failure, otherwise return success.
     */
    @Override
    public String getResultCode() {
        return wsAddMemberResults.getResultMetadata().getResultCode();
    }

    public Group getGroup() {
        return new Group(wsAddMemberResults.getWsGroupAssigned());
    }
}
