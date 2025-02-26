package edu.hawaii.its.api.wrapper;

import java.util.List;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

/**
 * A wrapper for GcFindGroups. When a group or grouping path is passed, FindGroupsCommand on execute
 * fetches(from grouper) results containing an existing or non-existing group pertaining to the path passed.
 */
public class FindGroupsCommand extends GrouperCommand<FindGroupsCommand> implements Command<FindGroupsResults> {
    private final GcFindGroups gcFindGroups;

    public FindGroupsCommand() {
        this.gcFindGroups = new GcFindGroups();
        this.gcFindGroups.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
        this.gcFindGroups.assignIncludeGroupDetail(true);
    }

    @Override
    protected FindGroupsCommand self() {
        return this;
    }

    @Override
    public FindGroupsResults execute() {
        WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
        return new FindGroupsResults(wsFindGroupsResults);
    }

    public FindGroupsCommand addPath(String path) {
        this.gcFindGroups.addGroupName(path);
        return this;
    }

    public FindGroupsCommand addPaths(List<String> paths) {
        if (paths.isEmpty()) {
            addPath("");
        }
        for (String path : paths) {
            addPath(path);
        }
        return this;
    }

    public FindGroupsCommand owner(String uhIdentifier) {
        this.gcFindGroups.assignActAsSubject(subjectLookup(uhIdentifier));
        return this;
    }
}
