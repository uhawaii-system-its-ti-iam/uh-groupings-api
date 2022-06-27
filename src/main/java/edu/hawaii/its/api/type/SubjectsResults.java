package edu.hawaii.its.api.type;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

public class SubjectsResults {

    public static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    private WsGetSubjectsResults wsGetSubjectsResults;
    public String[] args;

    // Constructor (temporary; don't depend on it).
    public SubjectsResults(WsGetSubjectsResults wsGetSubjectsResults) {
        this.wsGetSubjectsResults = wsGetSubjectsResults;
        if (this.wsGetSubjectsResults == null) {
            this.wsGetSubjectsResults = new WsGetSubjectsResults();
        }
    }

    public String getResultCode() {
        if (!hasItems(wsGetSubjectsResults.getWsSubjects())) {
            return SUBJECT_NOT_FOUND;
        }

        return wsGetSubjectsResults.getWsSubjects()[0].getResultCode();
    }

    public int getSubjectAttributeNameCount() {
        if (!hasItems(wsGetSubjectsResults.getSubjectAttributeNames())) {
            return 0;
        }
        return wsGetSubjectsResults.getSubjectAttributeNames().length;
    }

    public String getSubjectAttributeName(int index) {
        return wsGetSubjectsResults.getSubjectAttributeNames()[index];
    }

    public String getAttributeValue(int index) {
        return wsGetSubjectsResults.getWsSubjects()[0].getAttributeValues()[index];
    }

    private boolean hasItems(Object[] o) {
        return o != null && o.length > 0;
    }

}
