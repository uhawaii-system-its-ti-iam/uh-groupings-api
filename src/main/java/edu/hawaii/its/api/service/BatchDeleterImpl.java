package edu.hawaii.its.api.service;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Service("grouperFactoryThreading")
@Profile(value = { "localhost", "test", "integrationTest", "qa", "prod" })
public class BatchDeleterImpl implements BatchDeleter {

    /*Constructor for the Callable class used by makeWsBatchDeleteMemberResults*/
    public class MyCallable implements Callable {
        private final int currGroup;
        private final String userToRemove;
        private final List<String> groupPaths;
        private final GrouperFactoryServiceImpl gfs;

        public MyCallable(int currGroup, String userToRemove, List<String> groupPaths, GrouperFactoryServiceImpl gfs){
            this.currGroup = currGroup;
            this.userToRemove = userToRemove;
            this.groupPaths = groupPaths;
            this.gfs = gfs;
        }

        public WsDeleteMemberResults call(){
                WsDeleteMemberResults result = gfs.makeWsDeleteMemberResults(groupPaths.get(currGroup), userToRemove);
                return result;
        }
    }



    @Override
    public List<WsDeleteMemberResults> makeWsBatchDeleteMemberResults(List<String> GroupPaths, String userToRemove){

        List<WsDeleteMemberResults> results = new ArrayList<WsDeleteMemberResults>();

        // Creating a thread list which is populated with a thread for each removal that needs to be done.
        List<Thread> threads = new ArrayList<Thread>();
        List<FutureTask> tasks = new ArrayList<FutureTask>();
        for (int currGroup = 0; currGroup < GroupPaths.size(); currGroup++) {
            //creating runnable object containing the data needed for each individual delete.
            GrouperFactoryServiceImpl gfs = new GrouperFactoryServiceImpl();
            Callable master = new MyCallable(currGroup, userToRemove, GroupPaths, gfs);
            System.out.println("FutureTask " + currGroup + " created.");
            FutureTask task = new FutureTask(master);
            tasks.add(task);
            Thread curr = new Thread(task);
            threads.add(curr);
            System.out.println("Thread " + currGroup + " created.");
        }
        // Starting all of the created threads.
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).start();
        }
        // Waiting to return result until every thread in the list has completed running.
        for (int i = 0; i < threads.size(); i++) {
            try {
                System.out.println("Getting result from FutureTask " + i + ".");
                results.add((WsDeleteMemberResults)(tasks.get(i)).get());
                System.out.println("Added result from FutureTask " + i + " to results list.");
                threads.get(i).join();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Thread Interrupted.");
            }
        }
        return results;
    }
}
