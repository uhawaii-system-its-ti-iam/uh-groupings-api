package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public class SubjectsCommand extends GrouperCommand implements Command<SubjectsResults> {

    private GcGetSubjects gcGetSubjects;

    public SubjectsCommand(List<String> uhIdentifiers) {
        gcGetSubjects = new GcGetSubjects();
        gcGetSubjects.assignIncludeSubjectDetail(true);
        if (uhIdentifiers != null) {
            for (String uhIdentifier : uhIdentifiers) {
                addSubject(uhIdentifier);
            }
        }
    }

    public SubjectsResults execute() {
        SubjectsResults subjectsResults;
        try {
            WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
            JsonUtil.printJson(wsGetSubjectsResults);
            subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        } catch (RuntimeException e) {
            subjectsResults = new SubjectsResults();
        }
        return subjectsResults;
    }

    private SubjectsCommand addSubject(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = subjectLookup(uhIdentifier);
        gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
        return this;
    }
}
