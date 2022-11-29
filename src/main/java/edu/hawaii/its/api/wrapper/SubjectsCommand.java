package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public class SubjectsCommand extends GrouperCommand implements Command<SubjectsResults> {

    private GcGetSubjects gcGetSubjects;

    public SubjectsCommand(List<String> uhIdentifiers) {
        gcGetSubjects = new GcGetSubjects();
        for (String uhIdentifier : uhIdentifiers) {
            addSubject(uhIdentifier)
                    .addSubjectAttribute("uhUuid")
                    .addSubjectAttribute("uid")
                    .addSubjectAttribute("cn")
                    .addSubjectAttribute("sn")
                    .addSubjectAttribute("givenName");
        }
    }

    public SubjectsResults execute() {
        SubjectsResults subjectsResults;
        try {
            WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
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

    private SubjectsCommand addSubjectAttribute(String attribute) {
        gcGetSubjects.addSubjectAttributeName(attribute);
        return this;
    }
}
