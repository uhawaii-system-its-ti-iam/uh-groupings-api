package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

public class FindGroupResult extends Results {
    private final WsFindGroupsResults wsFindGroupsResults;
    private Group group;

    public FindGroupResult(WsFindGroupsResults wsFindGroupsResults) {
        if (wsFindGroupsResults == null) {
            this.wsFindGroupsResults = new WsFindGroupsResults();
            this.group = new Group();
        } else {
            this.wsFindGroupsResults = wsFindGroupsResults;
            if (isEmpty(this.wsFindGroupsResults.getGroupResults())) {
                this.group = new Group();
            } else {
                this.group = new Group(this.wsFindGroupsResults.getGroupResults()[0]);
            }
        }
    }

    public FindGroupResult() {
        this.wsFindGroupsResults = new WsFindGroupsResults();
    }

    public String getGroupPath() {
        return group.getGroupPath();
    }

    public String getDescription() {
        return group.getDescription();
    }

    public String getExtension() {
        return group.getExtension();
    }

    public boolean isValidPath() {
        return group.isValidPath();
    }

    @Override public String getResultCode() {
        return group.getResultCode();
    }

}
