package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.ArrayList;
import java.util.List;

public class SubjectsResults extends Results {

    public static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";
    public static final String SUCCESS= "SUCCESS";
    public static final String FAILURE= "FAILURE";

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

    public List<Subject> getSubjects() {
        List<Subject> subjects = new ArrayList<>();
        WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
        if (!isEmpty(wsSubjects)) {
            for (WsSubject wsSubject : wsSubjects) {
                subjects.add(new Subject(wsSubject));
            }
        }
        return subjects;
    }

    @Override
    public String getResultCode() {
        List<Subject> subjects = getSubjects();
        if (subjects.isEmpty()) {
            return FAILURE;
        }
        for (Subject subject : subjects) {
            if (subject.getResultCode().equals(SUBJECT_NOT_FOUND)) {
                return FAILURE;
            }
        }
        return SUCCESS;
    }
}
