package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsResponseMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Value;

public class MembershipServiceUnitTest {

    @Value("${groupings.api.success_allowed}")
    private String SUCCESS_ALLOWED;

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
    public void batchIsMemberTest() {
        MemberAttributeService memberAttribute = new MemberAttributeServiceImpl() {
            public boolean isMember(String groupPath, Person personToAdd) {
                System.out.println("Checking if " + personToAdd + " is a member of " + groupPath);
                return true;
            }
            public boolean isOwner(String groupPath, String personToAdd) {
                System.out.println("Checking if " + personToAdd + " is a owner of " + groupPath);
                return true;
            }
            public boolean isSuperuser(String username) {
                System.out.println("Checking if " + username + " is a super user.");
                return true;
            }
        };

        GrouperFactoryService grouperService = new GrouperFactoryServiceImpl() {
            public WsGetGrouperPrivilegesLiteResult makeWsGetGrouperPrivilegesLiteResult(String groupName,
                    String privilegeName,
                    WsSubjectLookup lookup) {
                WsGetGrouperPrivilegesLiteResult result = new WsGetGrouperPrivilegesLiteResult();
                WsResultMeta res = new WsResultMeta();
                res.setResultCode("${groupings.api.success_allowed}");
                result.setResultMetadata(res);
                return result;
            }
        };

        HelperService helperService = new HelperServiceImpl() {
            public GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action) {
                GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
                groupingsServiceResult.setAction(action);
                groupingsServiceResult.setResultCode(resultCode);
                return groupingsServiceResult;
            }
        };


        //MembershipServiceImpl membershipService = mock(MembershipServiceImpl.class);
        //when(membershipService.isGroupCanOptIn("","")).thenReturn(true);
        //when(membershipService.optIn(optInUsername,groupPaths.get(j))).thenCallRealMethod();
        MembershipServiceImpl membershipService = new MembershipServiceImpl();
        membershipService.setMemberAttributeService(memberAttribute);
        membershipService.setGrouperFactoryService(grouperService);
        membershipService.setHelperService(helperService);
        List<String> groupPaths = new ArrayList<>();
        List<List<GroupingsServiceResult>> memberResults = new ArrayList<>();

        // Creating 5 groupPaths.
        for (int i = 0; i < 100; i++) {
            groupPaths.add("groupPath-" + i);
            // Creating 5 users to batch delete from those 5 groups.
        }
        for (int j = 0; j < 100; j++) {
            String optInUsername = "optInUsername-" + j;
            memberResults.add(membershipService.optIn(optInUsername, groupPaths.get(j)));
        }

        // Check that all threads returned a result.
        assertEquals(memberResults.size(), 100);
        int i = 0;
        // Check that the proper result information is returned.
        for (List<GroupingsServiceResult> result : memberResults){
           assertEquals(result.get(0).getAction(), "opt in optInUsername-"+ i +" to groupPath-" + i);
           i++;
        }


    }

    private class DeleteTestRunnerTwo extends Thread {
        private final int id;
        private final MembershipServiceImpl membershipService;
        public DeleteTestRunnerTwo(int id, MembershipServiceImpl membershipService) {
            this.id = id;
            this.membershipService = membershipService;
        }
        @Override
        public void run() {
            try {
                System.out.println("Running " + id);
                List<String> groupPaths = Arrays.asList("groupPath-" + id);
                String userToRemove = "userToRemove-" + id;
                membershipService.makeWsBatchDeleteMemberResults(groupPaths, userToRemove);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void batchDeleteThree() {
        final int TEST_RUNS = 30;
        final Map<String, String> mapControl = new HashMap<>(0);
        final Map<String, String> map = new HashMap<>(0);
        GrouperFactoryService grouperService = new GrouperFactoryServiceImpl() {
            public WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete) {
                System.out.println("DELETING " + memberToDelete + " from " + group);
                synchronized (map) {
                    map.put(group, memberToDelete);
                }
                return new WsDeleteMemberResults();
            }
        };
        MembershipServiceImpl membershipService = new MembershipServiceImpl();
        membershipService.setGrouperFactoryService(grouperService);
        Thread[] serviceThreads = new DeleteTestRunnerTwo[TEST_RUNS];
        for (int i = 0; i < TEST_RUNS; i++) {
            serviceThreads[i] = new DeleteTestRunnerTwo(i, membershipService);
            serviceThreads[i].start();
        }
        // Wait on the threads to finish.
        pollUntilFinished(TEST_RUNS, serviceThreads);
        assertThat(map.size(), equalTo(TEST_RUNS));
        // Check the results.
        // TODO: check the group and memberToDelete values.

        int expectedGroup = 0;
        int expectedMember = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println("map.entry; key: " + e.getKey() + ", value: " + e.getValue());
            int i = 0;
            for (Map.Entry<String, String> d : map.entrySet()) {
                if(e.getValue().equals("userToRemove-" + i)){
                    expectedMember++;
                }
                if(e.getKey().equals("groupPath-" + i)){
                    expectedGroup++;
                }
                i++;
            }
        }
        //Verifying that all the expected results are within the hash map.
        assertEquals(map.size(), expectedGroup);
        assertEquals(map.size(), expectedMember);
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

    class testCallable1 implements Callable<Integer> {
        private final int increment;
        public testCallable1(int increment) {
            this.increment = increment;
        }
        @Override
        public Integer call() {
            int i = increment + 1;
            return i;
        }
    }


    @Test
    public void executorServiceTest() throws ExecutionException, InterruptedException {
        // Creates a thread pool with 10 threads.
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Utilize constructors to configure custom executor service.
        ExecutorService executorService =
                new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());

        List<Callable<Integer>> threads = new ArrayList<>();
        for(int i = 0; i <= 100; i++){
            Callable<Integer> test = new testCallable1(i);
            threads.add(test);
        }
        // Send all threads to the executor service which runs all threads with invokeAll.
        List<Future<Integer>> results = executor.invokeAll(threads);
        int expectedValue = 1;

        // Asserts that all threads have incremented their passed int by 1.
        for (Future result : results){
            assertEquals(result.get(), expectedValue);
            expectedValue++;
        }



        // Shuts down the service once all threads have completed.
        executor.shutdown();

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