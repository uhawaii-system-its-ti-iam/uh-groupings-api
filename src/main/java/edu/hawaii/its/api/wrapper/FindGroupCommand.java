package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

import java.util.Objects;

public class FindGroupCommand extends GrouperCommand implements Command<FindGroupResult> {
    private final GcFindGroups gcFindGroups;

    public FindGroupCommand(String path) {
        Objects.requireNonNull(path, "path cannot be null");
        gcFindGroups = new GcFindGroups();
        this.addPath(path);
    }

    private FindGroupCommand addPath(String path) {
        this.gcFindGroups.addGroupName(path);
        return this;
    }

    @Override public FindGroupResult execute() {
        FindGroupResult findGroupResult ;
        try {
            WsFindGroupsResults wsFindGroupsResults = gcFindGroups.execute();
            findGroupResult = new FindGroupResult(wsFindGroupsResults);
        }catch (RuntimeException e) {
            findGroupResult = new FindGroupResult();
        }
        return findGroupResult;
    }
}
