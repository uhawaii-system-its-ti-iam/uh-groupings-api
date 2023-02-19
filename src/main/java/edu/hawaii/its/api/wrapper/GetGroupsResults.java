package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.ArrayList;
import java.util.List;

public class GetGroupsResults extends Results {
    private final WsGetGroupsResults wsGetGroupsResults;

    private List<Group> groups;

    private Subject subject;

    public GetGroupsResults(WsGetGroupsResults wsGetGroupsResults) {
        groups = new ArrayList<>();
        if (wsGetGroupsResults == null) {
            this.wsGetGroupsResults = new WsGetGroupsResults();
        } else {
            this.wsGetGroupsResults = wsGetGroupsResults;
            setGroups();
            setSubject();
        }
    }

    @Override public String getResultCode() {
        return wsGetGroupsResults.getResultMetadata().getResultCode();
    }

    private void setGroups() {
        if (isEmpty(wsGetGroupsResults.getResults())) {
            return;
        }
        WsGroup[] wsGroups = wsGetGroupsResults.getResults()[0].getWsGroups();
        if (isEmpty(wsGroups)) {
            return;
        }
        for (WsGroup wsGroup : wsGroups) {
            groups.add(new Group(wsGroup));
        }
    }

    private void setSubject() {
        if (isEmpty(wsGetGroupsResults.getResults())) {
            this.subject = new Subject();
            return;
        }
        WsSubject wsSubject = wsGetGroupsResults.getResults()[0].getWsSubject();
        if (wsSubject == null) {
            this.subject = new Subject();
            return;
        }
        subject = new Subject(wsSubject);
    }

    public Subject getSubject() {
        return subject;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
