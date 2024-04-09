package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

public class FindGroupsResults extends Results {

    private final WsFindGroupsResults wsFindGroupsResults;

    public FindGroupsResults(WsFindGroupsResults wsFindGroupsResults) {
        if (wsFindGroupsResults == null) {
            this.wsFindGroupsResults = new WsFindGroupsResults();
        } else {
            this.wsFindGroupsResults = wsFindGroupsResults;
        }
    }

    public FindGroupsResults() {
        this.wsFindGroupsResults = new WsFindGroupsResults();
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
        WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
        List<Group> groups = new ArrayList<>();
        if (!isEmpty(wsGroups)) {
            for (WsGroup wsGroup : wsGroups) {
                groups.add(new Group(wsGroup));
            }
        }
        return groups;
    }

    public Group getGroup() {
        WsGroup[] wsGroups = wsFindGroupsResults.getGroupResults();
        if (isEmpty(wsGroups)) {
            return new Group();
        }
        return new Group(wsGroups[0]);
    }

    @JsonIgnore
    public WsFindGroupsResults getWsFindGroupsResults() {
        return wsFindGroupsResults;
    }

}
