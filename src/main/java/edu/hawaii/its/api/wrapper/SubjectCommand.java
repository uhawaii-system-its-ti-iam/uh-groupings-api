package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.Objects;

public class SubjectCommand extends GrouperCommand implements Command<SubjectResult> {

    private final GcGetSubjects gcGetSubjects;

    public SubjectCommand(String uhIdentifier) {
        Objects.requireNonNull(uhIdentifier, "uhIdentifier cannot be null");
        gcGetSubjects = new GcGetSubjects();
        gcGetSubjects.assignIncludeSubjectDetail(true);
        this.addSubject(uhIdentifier)
                .addSubjectAttribute("uhUuid")
                .addSubjectAttribute("uid")
                .addSubjectAttribute("cn")
                .addSubjectAttribute("sn")
                .addSubjectAttribute("givenName");
    }

    public SubjectResult execute() {
        SubjectResult subjectResult;
        WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
        return new SubjectResult(wsGetSubjectsResults);
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
