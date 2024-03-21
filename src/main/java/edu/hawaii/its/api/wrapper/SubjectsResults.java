package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
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
        String success = "SUCCESS";
        String failure = "FAILURE";
        for (Subject subject : getSubjects()) {
            if (subject.getResultCode().equals(success)) {
                return success;
            }
        }
        return failure;
    }

    public void getSubjectsAfterAssignSubject(String uhIdentifier){
        WsSubject newSubject = new WsSubject();
        newSubject.setIdentifierLookup(uhIdentifier);
        newSubject.setId(uhIdentifier);
        newSubject.setResultCode("SUCCESS");
        WsSubject[] updatedSubjects = new WsSubject[]{newSubject};
        this.wsGetSubjectsResults.setWsSubjects(updatedSubjects);
    }

    public void getSubjectsAfterAssignSubjects(List<String> uhIdentifiers){
        WsGetSubjectsResults wsGetSubjectsResults = this.wsGetSubjectsResults;
        WsSubject[] updatedWsSubjects = uhIdentifiers.stream()
                .map(uhIdentifier -> {
                    WsSubject wsSubject = new WsSubject();
                    wsSubject.setIdentifierLookup(uhIdentifier);
                    wsSubject.setId(uhIdentifier);
                    wsSubject.setResultCode("SUCCESS");
                    return wsSubject;
                })
                .toArray(WsSubject[]::new);
        wsGetSubjectsResults.setWsSubjects(updatedWsSubjects);
    }
}
