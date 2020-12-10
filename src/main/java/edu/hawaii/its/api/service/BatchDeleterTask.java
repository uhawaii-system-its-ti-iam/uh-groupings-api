package edu.hawaii.its.api.service;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.util.concurrent.Callable;

public class BatchDeleterTask implements Callable<WsDeleteMemberResults> {

    //BatchDeleterTask Values.
    private final String userToRemove;
    private final String groupPath;
    private final BatchDeleter bd;

    public BatchDeleterTask(String userToRemove, String groupPath, BatchDeleter bd) {
        this.userToRemove = userToRemove;
        this.groupPath = groupPath;
        this.bd = bd;
    }

    @Override
    public WsDeleteMemberResults call() {
        return bd.makeWsDeleteMemberResults(groupPath, userToRemove);
    }
}