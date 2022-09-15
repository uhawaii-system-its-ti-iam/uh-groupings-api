package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

public class SubjectCommand extends GrouperCommand implements Command<SubjectResult> {

    private final GcGetSubjects gcGetSubjects;

    public SubjectCommand(String uhIdentifier) {
        gcGetSubjects = new GcGetSubjects();
        this.addSubject(uhIdentifier)
                .addSubjectAttribute("uhUuid")
                .addSubjectAttribute("uid")
                .addSubjectAttribute("cn")
                .addSubjectAttribute("sn")
                .addSubjectAttribute("givenName");
    }

    public SubjectResult execute() {
        SubjectResult subjectResult;
        try {
            WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
            subjectResult = new SubjectResult(wsGetSubjectsResults);
        } catch (RuntimeException e) {
            subjectResult = new SubjectResult();
        }
        return subjectResult;
    }

    private SubjectCommand addSubject(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = subjectLookup(uhIdentifier);
        gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
        return this;
    }

    private SubjectCommand addSubjectAttribute(String attribute) {
        gcGetSubjects.addSubjectAttributeName(attribute);
        return this;
    }

}
