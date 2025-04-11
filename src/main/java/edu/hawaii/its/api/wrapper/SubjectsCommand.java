package edu.hawaii.its.api.wrapper;

import java.util.List;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * A wrapper for GcGetSubjects. When a string is passed as a UH identifier, SubjectsCommand on execute fetches, from
 * grouper, results containing all the UH attributes pertaining to that UH identifier. Multiple strings can be queried
 * thus multiple UH affiliates can be validated in one call to execute. Unlike, some other Grouper GC classes,
 * passing an invalid UH identifier on execute does not throw a RuntimeException, thus making GcGetSubjects a great
 * candidate for checking that validly of UH identifier before it used to query grouper for add and remove.
 */
public class SubjectsCommand extends GrouperCommand<SubjectsCommand> implements Command<SubjectsResults> {
    private final GcGetSubjects gcGetSubjects;

    public SubjectsCommand() {
        this.gcGetSubjects = new GcGetSubjects();
        this.gcGetSubjects.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
        this.gcGetSubjects.assignIncludeSubjectDetail(true);
    }

    @Override
    public SubjectsResults execute() {
        WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
        return new SubjectsResults(wsGetSubjectsResults);
    }

    @Override
    protected SubjectsCommand self() {
        return this;
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

    public SubjectsCommand assignGroupingPath(String groupingPath) {
        this.gcGetSubjects.assignGroupLookup(groupLookup(groupingPath));
        return this;
    }

    public SubjectsCommand assignSearchString(String searchString) {
        this.gcGetSubjects.assignSearchString(searchString);
        return this;
    }
}
