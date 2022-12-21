package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;
import java.util.Objects;

public class SubjectsCommand extends GrouperCommand implements Command<SubjectsResults> {

    private final GcGetSubjects gcGetSubjects;

    public SubjectsCommand(List<String> uhIdentifiers) {
        Objects.requireNonNull(uhIdentifiers, "uhIdentifiers cannot be null");
        gcGetSubjects = new GcGetSubjects();
        gcGetSubjects.assignIncludeSubjectDetail(true);
        for (String uhIdentifier : uhIdentifiers) {
            Objects.requireNonNull(uhIdentifier, "uhIdentifier cannot be null");
            addSubject(uhIdentifier);
        }
    }

    public SubjectsResults execute() {
        SubjectsResults subjectsResults;
        WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
        return new SubjectsResults(wsGetSubjectsResults);
    }

    private SubjectsCommand addSubject(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = subjectLookup(uhIdentifier);
        gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
        return this;
    }
}
