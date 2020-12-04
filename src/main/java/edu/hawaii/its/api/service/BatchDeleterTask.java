package edu.hawaii.its.api.service;

import java.util.List;
import java.util.concurrent.Callable;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class BatchDeleterTask implements Callable<WsDeleteMemberResults> {

    private final int currGroup;
    private final String userToRemove;
    private final List<String> groupPaths;
    private final BatchDeleter bd;

    public BatchDeleterTask(int currGroup, String userToRemove, List<String> groupPaths, BatchDeleter bd) {
        this.currGroup = currGroup;
        this.userToRemove = userToRemove;
        this.groupPaths = groupPaths;
        this.bd = bd;
    }

    @Override
    public WsDeleteMemberResults call() {
        return bd.makeWsDeleteMemberResults(groupPaths.get(currGroup), userToRemove);
    }
}
