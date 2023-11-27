package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

public class GroupsResults {
    private WsGetGroupsResults groupsResults;

    public GroupsResults(WsGetGroupsResults wsGetGroupsResults) {
        if (wsGetGroupsResults == null) {
            groupsResults = new WsGetGroupsResults();
        } else {
            groupsResults = wsGetGroupsResults;
        }
    }

    public List<String> groupPaths() {
        List<String> results = new ArrayList<>();

        if (isEmpty(groupsResults.getResults())) {
            return results;
        }

        WsGroup[] groups = groupsResults.getResults()[0].getWsGroups();
        if (isEmpty(groups)) {
            return results;
        }
        Set<String> groupNames = new LinkedHashSet<>();
        for (WsGroup group : groups) {
            groupNames.add(group.getName());
        }

        results = new ArrayList<>(groupNames);
        return results;
    }

    private boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }
}
