package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

import java.util.ArrayList;
import java.util.List;

public class FindGroupsResults extends Results {
    private final WsFindGroupsResults wsFindGroupsResults;

    private final List<Group> groups;

    public FindGroupsResults(WsFindGroupsResults wsFindGroupsResults) {
        groups = new ArrayList<>();
        if (wsFindGroupsResults == null) {
            this.wsFindGroupsResults = new WsFindGroupsResults();
        } else {
            this.wsFindGroupsResults = wsFindGroupsResults;
            setGroups();
        }
    }

    @Override public String getResultCode() {
        for (Group group : getGroups()) {
            if (group.getResultCode().equals("SUCCESS")) {
                return "SUCCESS";
            }
        }
        return "FAILURE";
    }

    public List<Group> getGroups() {
        return groups;
    }

    private void setGroups() {
        WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
        if (isEmpty(wsGroups)) {
            return;
        }
        for (WsGroup wsGroup : wsGroups) {
            groups.add(new Group(wsGroup));
        }
    }

    public Group getGroup() {
        WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
        if (isEmpty(wsGroups)) {
            return new Group();
        }
        return new Group(wsGroups[0]);
    }

}
