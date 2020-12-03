package edu.hawaii.its.api.service;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BatchDeleterTask implements Callable<WsDeleteMemberResults> {

        private final int currGroup;
        private final String userToRemove;
        private final List<String> groupPaths;
        private final BatchDeleter bd;

        public BatchDeleterTask(int currGroup, String userToRemove, List<String> groupPaths, BatchDeleter bd){
            this.currGroup = currGroup;
            this.userToRemove = userToRemove;
            this.groupPaths = groupPaths;
            this.bd = bd;
        }

        @Override
        public WsDeleteMemberResults call(){
                return bd.makeWsDeleteMemberResults(groupPaths.get(currGroup), userToRemove);
        }

}
