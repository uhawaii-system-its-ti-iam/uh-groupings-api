package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

import java.util.List;
import java.util.Objects;

/**
 * A wrapper for GcFindGroups. When a group or grouping path is passed, FindGroupsCommand on execute
 * fetches(from grouper) results containing an existing or non-existing group pertaining to the path passed.
 */
public class FindGroupsCommand extends GrouperCommand implements Command<FindGroupsResults> {
    private final GcFindGroups gcFindGroups;

    public FindGroupsCommand() {
        gcFindGroups = new GcFindGroups();
        gcFindGroups.assignIncludeGroupDetail(true);
    }

    public FindGroupsCommand(String path) {
        this();
        Objects.requireNonNull(path, "path cannot be null");
        this.addPath(path);
    }

    public FindGroupsCommand(List<String> paths) {
        this();
        Objects.requireNonNull(paths, "paths cannot be null");
        for (String path : paths) {
            this.addPath(path);
        }
    }

    @Override
    public FindGroupsResults execute() {
        WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
        return new FindGroupsResults(wsFindGroupsResults);
    }


    private FindGroupsCommand addPath(String path) {
        this.gcFindGroups.addGroupName(path);
        return this;
    }
}
