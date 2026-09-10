package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

public class GetGroupsResults extends Results {

    private final WsGetGroupsResults wsGetGroupsResults;

    public GetGroupsResults(WsGetGroupsResults wsGetGroupsResults) {
        if (wsGetGroupsResults == null) {
            this.wsGetGroupsResults = new WsGetGroupsResults();
        } else {
            this.wsGetGroupsResults = wsGetGroupsResults;
        }
    }

    public GetGroupsResults() {
        this.wsGetGroupsResults = new WsGetGroupsResults();
    }

    @Override public String getResultCode() {
        if (isEmpty(wsGetGroupsResults.getResults())) {
            return "FAILURE";
        }
        if (isEmpty(wsGetGroupsResults.getResults()[0].getWsGroups())) {
            return "FAILURE";
        }
        return wsGetGroupsResults.getResultMetadata().getResultCode();
    }

    public List<Group> getGroups() {
        WsGetGroupsResult[] wsGetGroupsResults = this.wsGetGroupsResults.getResults();
        List<Group> groups = new ArrayList<>();
        if (!isEmpty(wsGetGroupsResults)) {
            WsGroup[] wsGroups = wsGetGroupsResults[0].getWsGroups();
            if (!isEmpty(wsGroups)) {
                for (WsGroup wsGroup : wsGroups) {
                    groups.add(new Group(wsGroup));
                }
            }
        }
        return groups;
    }

    /**
     * The groups that each subject of the query is listed in, keyed on subject name. When the subjects are groups,
     * the subject name is the group path.
     */
    public Map<String, List<Group>> getGroupsBySubjectName() {
        WsGetGroupsResult[] results = this.wsGetGroupsResults.getResults();
        Map<String, List<Group>> groupsBySubjectName = new HashMap<>();
        if (isEmpty(results)) {
            return groupsBySubjectName;
        }
        for (WsGetGroupsResult result : results) {
            WsSubject wsSubject = result.getWsSubject();
            if (wsSubject == null || wsSubject.getName() == null) {
                continue;
            }
            List<Group> groups = new ArrayList<>();
            WsGroup[] wsGroups = result.getWsGroups();
            if (!isEmpty(wsGroups)) {
                for (WsGroup wsGroup : wsGroups) {
                    groups.add(new Group(wsGroup));
                }
            }
            groupsBySubjectName.put(wsSubject.getName(), groups);
        }
        return groupsBySubjectName;
    }

    public Subject getSubject() {
        WsGetGroupsResult[] wsGetGroupsResults = this.wsGetGroupsResults.getResults();
        if (isEmpty(wsGetGroupsResults)) {
            return new Subject();
        }
        WsSubject wsSubject = wsGetGroupsResults[0].getWsSubject();
        return wsSubject != null ? new Subject(wsSubject) : new Subject();

    }

    @JsonIgnore
    public WsGetGroupsResults getWsGetGroupsResults() {
        return wsGetGroupsResults;
    }
}
