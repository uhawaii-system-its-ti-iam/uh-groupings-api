package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

/**
 * A wrapper for GcGetSubjects. When a string is passed as a UH identifier, SubjectsCommand on execute fetches, from
 * grouper, results containing all the UH attributes pertaining to that UH identifier. Multiple strings can be queried
 * thus multiple UH affiliates can be validated in one call to execute. Unlike, some of the other Grouper GC classes,
 * passing an invalid UH identifier on execute does not throw a RuntimeException, thus making GcGetSubjects a great
 * candidate for checking that validly of UH identifier before it used to query grouper for add and remove.
 */
public class SubjectsCommand extends GrouperCommand implements Command<SubjectsResults> {
    private final GcGetSubjects gcGetSubjects;

    public SubjectsCommand() {
        gcGetSubjects = new GcGetSubjects();
        gcGetSubjects.assignIncludeSubjectDetail(true);
    }

    public SubjectsResults execute() {
        WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
        return new SubjectsResults(wsGetSubjectsResults);
    }

    public SubjectsCommand addSubject(String uhIdentifier) {
        WsSubjectLookup wsSubjectLookup = subjectLookup(uhIdentifier);
        gcGetSubjects.addWsSubjectLookup(wsSubjectLookup);
        return this;
    }

    public SubjectsCommand addSubjects(List<String> uhIdentifiers) {
        for (String uhIdentifier : uhIdentifiers) {
            gcGetSubjects.addWsSubjectLookup(subjectLookup(uhIdentifier));
        }
        return this;
    }

    public SubjectsCommand addSubjectAttribute(String attribute) {
        gcGetSubjects.addSubjectAttributeName(attribute);
        return this;
    }
}
