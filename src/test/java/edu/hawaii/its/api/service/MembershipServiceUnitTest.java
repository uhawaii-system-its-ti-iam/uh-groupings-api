package edu.hawaii.its.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class MembershipServiceUnitTest {

    @Test
    public void batchDeleteOne() {
        GrouperFactoryService grouperService = new GrouperFactoryServiceImpl() {
            public WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete) {
                System.out.println("DELETING " + memberToDelete + " from " + group);
                return new WsDeleteMemberResults();

            }
        };
        MembershipServiceImpl membershipService = new MembershipServiceImpl();
        membershipService.setGrouperFactoryService(grouperService);
        List<String> groupPaths = new ArrayList<>();
        List<WsDeleteMemberResults> memberResults = new ArrayList<>();
        //Creating 5 groupPaths.
        for (int i = 0; i < 5; i++) {
            groupPaths.add("groupPath-" + i);
            //Creating 5 users to batch delete from those 5 groups.
        }
        for (int j = 0; j < 5; j++) {
            String userToRemove = "userToRemove-" + j;
            memberResults.addAll(membershipService.makeWsBatchDeleteMemberResults(groupPaths, userToRemove));
        }

        //Validates non-concurrent part of threading proving that the amount of member results returned equals the amount of deletes that occurred.
        assertTrue(memberResults.size() == 25);

        //Validates that every returned result is a memberResult, proving all of the threads are working properly.
        memberResults.forEach(
                result -> assertTrue((result.toString())
                        .contains("edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults@")));
    }

    @Test
    public void batchDeleteTwo() {
        final int TEST_RUNS = 25;
        final Map<Integer, Integer> map = new HashMap<>();

        Thread[] serviceThreads = new DeleteTestRunner[TEST_RUNS];
        for (int i = 0; i < TEST_RUNS; i++) {
            serviceThreads[i] = new DeleteTestRunner(i, map);
            serviceThreads[i].start();
        }

        // Wait on the threads to finish.
        pollUntilFinished(TEST_RUNS, serviceThreads);

        // Check the results.
        for (int i = 0; i < TEST_RUNS; i++) {
            assertEquals(map.get(i).intValue(), i + 100);
        }
    }

    private synchronized void pollUntilFinished(final int TEST_RUNS, Thread[] serviceThreads) {
        // Poll the threads to see if they are done.
        boolean isFinished = false;
        while (!isFinished) {
            isFinished = true;
            for (int i = 0; i < TEST_RUNS; i++) {
                if (serviceThreads[i].isAlive()) {
                    sleep(50);
                    isFinished = false;
                    break;
                }
            }
        }
    }

    private void sleep(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            System.out.println("Interrupted during sleep.");
        }
    }

    private class DeleteTestRunner extends Thread {
        private final int id;
        private final Map<Integer, Integer> map;

        public DeleteTestRunner(int id, Map<Integer, Integer> map) {
            this.id = id;
            this.map = map;
        }

        @Override
        public void run() {
            try {
                System.out.println("Running " + id);
                map.put(id, id + 100);
                // Remove above and put batch delete call.
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}