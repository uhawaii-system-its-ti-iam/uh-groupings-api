package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class SubjectResult extends Results {

    public static final String SUCCESS = "SUCCESS";
    public static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    private WsGetSubjectsResults wsGetSubjectsResults;
    private Subject subject;

    // Constructor (temporary; don't depend on it).
    public SubjectResult(WsGetSubjectsResults wsGetSubjectsResults) {
        this.wsGetSubjectsResults = wsGetSubjectsResults;
        if (this.wsGetSubjectsResults == null) {
            this.wsGetSubjectsResults = new WsGetSubjectsResults();
        }
        setSubject();
    }

    public SubjectResult() {
        this.wsGetSubjectsResults = new WsGetSubjectsResults();
    }

    public String getResultCode() {
        if (isEmpty(wsGetSubjectsResults.getWsSubjects())) {
            return SUBJECT_NOT_FOUND;
        }
        return getSubject().getResultCode();
    }

    public String getUhUuid() {
        return subject.getUhUuid();
    }

    public String getUid() {
        return subject.getUid();
    }

    public String getName() {
        return subject.getName();
    }

    public String getLastName() {
        return subject.getLastName();
    }

    public String getFirstName() {
        return subject.getFirstName();
    }

    public Subject getSubject() {
        return subject;
    }

    private void setSubject() {
        if (isEmpty(wsGetSubjectsResults.getWsSubjects())) {
            this.subject = new Subject(new WsSubject());
        } else {
            this.subject = new Subject(this.wsGetSubjectsResults.getWsSubjects()[0]);
        }
    }
}
