package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class RemoveMembersResults extends Results {

    private final WsDeleteMemberResults wsDeleteMemberResults;

    public RemoveMembersResults(WsDeleteMemberResults wsDeleteMemberResults) {
        if (wsDeleteMemberResults == null) {
            this.wsDeleteMemberResults = new WsDeleteMemberResults();
        } else {
            this.wsDeleteMemberResults = wsDeleteMemberResults;
        }
    }

    public RemoveMembersResults() {
        this.wsDeleteMemberResults = new WsDeleteMemberResults();
    }

    /**
     * Merge the results of a removal that was sent to Grouper in batches into one result, in batch order, as if
     * the removal had been sent in a single request.
     */
    public static RemoveMembersResults merge(List<RemoveMembersResults> batchResults) {
        if (batchResults.isEmpty()) {
            return new RemoveMembersResults();
        }
        if (batchResults.size() == 1) {
            return batchResults.get(0);
        }
        List<WsDeleteMemberResults> batches =
                batchResults.stream().map(results -> results.wsDeleteMemberResults).toList();
        WsDeleteMemberResults merged = new WsDeleteMemberResults();
        merged.setResults(batches.stream()
                .map(WsDeleteMemberResults::getResults)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toArray(WsDeleteMemberResult[]::new));
        merged.setWsGroup(batches.get(0).getWsGroup());
        merged.setSubjectAttributeNames(batches.get(0).getSubjectAttributeNames());
        merged.setResultMetadata(
                mergedResultMetadata(batches.stream().map(WsDeleteMemberResults::getResultMetadata).toList()));
        return new RemoveMembersResults(merged);
    }

    public String getGroupPath() {
        return getGroup().getGroupPath();
    }

    public Group getGroup() {
        return new Group(wsDeleteMemberResults.getWsGroup());
    }

    public List<RemoveMemberResult> getResults() {
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        WsDeleteMemberResult[] wsDeleteMemberResults = this.wsDeleteMemberResults.getResults();
        if (isEmpty(wsDeleteMemberResults)) {
            return removeMemberResults;
        }
        for (WsDeleteMemberResult wsDeleteMemberResult : wsDeleteMemberResults) {
            removeMemberResults.add(new RemoveMemberResult(wsDeleteMemberResult, getGroupPath()));
        }
        return removeMemberResults;
    }

    @Override
    public String getResultCode() {
        return wsDeleteMemberResults.getResultMetadata().getResultCode();
    }
}
