package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.type.GroupType;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A wrapper for WsGetSubjectsResults, which is returned from grouper when GcGetSubjects.execute(wrapped by
 * SubjectsCommand) is called. WsGetSubjectsResults contains a list of WsSubject(wrapped by Subject), for each UH
 * identifier queried a WsSubject is added to the list of WsSubject.
 */
public class SubjectsResults extends Results {
    private WsGetSubjectsResults wsGetSubjectsResults;

    public SubjectsResults(WsGetSubjectsResults wsGetSubjectsResults) {
        if (wsGetSubjectsResults == null) {
            this.wsGetSubjectsResults = new WsGetSubjectsResults();
        } else {
            this.wsGetSubjectsResults = wsGetSubjectsResults;
        }
    }

    public SubjectsResults() {
        this.wsGetSubjectsResults = new WsGetSubjectsResults();
    }

    public Group getGroup() {
        WsGroup wsGroup = wsGetSubjectsResults.getWsGroup();
        if (wsGroup == null) {
            return new Group();
        }
        return new Group(wsGroup);
    }

    public List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<>();
        WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
        if (isEmpty(wsSubjects)) {
            return subjects;
        }
        String groupPath = getGroup().getGroupPath();
        for (WsSubject wsSubject : wsSubjects) {
            if (groupPath.endsWith(GroupType.BASIS.value()) && wsSubject.getSourceId() != null
                    && wsSubject.getSourceId().equals("g:gsa")) {
                continue;
            }
            Subject subject = new Subject(wsSubject);
            if (subject.getResultCode().equals("SUCCESS") && !subject.hasUHAttributes()) {
                continue;
            }
            subjects.add(new Subject(wsSubject));
        }
        return subjects;
    }

    @Override
    public String getResultCode() {
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (Subject subject : getSubjects()) {
            if (subject.getResultCode().equals(success)) {
                return success;
            }
        }
        return failure;
    }

    @JsonIgnore
    public WsGetSubjectsResults getWsGetSubjectsResults() {
        return this.wsGetSubjectsResults;
    }
}
