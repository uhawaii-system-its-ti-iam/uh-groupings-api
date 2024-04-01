package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A wrapper for WsGetMembersResults, which is returned from grouper when GcGetMembers.execute(wrapped by GetMembersCommand)
 * is called. WsGetMembersResults contains a list of WsGetMembersResult, for each group path queried am WsGetMembersResult
 * is added to the list of WsGetMembersResults.
 */
public class GetMembersResults extends Results {

    private final WsGetMembersResults wsGetMembersResults;

    public GetMembersResults(WsGetMembersResults wsGetMembersResults) {
        if (wsGetMembersResults == null) {
            this.wsGetMembersResults = new WsGetMembersResults();
        } else {
            this.wsGetMembersResults = wsGetMembersResults;
        }
    }

    public GetMembersResults() {
        this.wsGetMembersResults = new WsGetMembersResults();
    }

    @Override public String getResultCode() {
        String resultCode = wsGetMembersResults.getResultMetadata().getResultCode();
        return resultCode != null ? resultCode : "FAILURE";
    }

    public List<GetMembersResult> getMembersResults() {
        List<GetMembersResult> getMembersResults = new ArrayList<>();
        WsGetMembersResult[] wsGetMembersResults = this.wsGetMembersResults.getResults();
        if (isEmpty(wsGetMembersResults)) {
            return getMembersResults;
        }
        for (WsGetMembersResult wsGetMembersResult : wsGetMembersResults) {
            getMembersResults.add(new GetMembersResult(wsGetMembersResult));
        }
        return getMembersResults;
    }

    public void removeMember(String groupPath, String uhIdentifier) {
        WsGetMembersResults wsGetMembersResults = this.wsGetMembersResults;
        WsGetMembersResult[] wsGetMembers = wsGetMembersResults.getResults();
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembers)
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifier.equals(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        wsGetMembersResults.setResults(updatedWsGetMembersResults);
    }

    public void removeMembers(String groupPath, List<String> uhIdentifiers) {
        WsGetMembersResults wsGetMembersResults = this.wsGetMembersResults;
        WsGetMembersResult[] wsGetMembers = wsGetMembersResults.getResults();
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembers)
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifiers.contains(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        wsGetMembersResults.setResults(updatedWsGetMembersResults);
    }

    public GetMembersResult getMembersByGroupPath(String groupPath){
        Optional<GetMembersResult> matchingResult = getMembersResults().stream()
                .filter(result -> groupPath.equals(result.getGroup().getGroupPath()))
                .findFirst();
        return matchingResult.orElse(null);
    }
}
