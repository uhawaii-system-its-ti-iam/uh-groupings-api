package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.Results;

import edu.internet2.middleware.grouperClient.GrouperClientWsException;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

public class BatchExecutorTest {

    // With a batch size of 2, these 5 identifiers are sent as 3 batches.
    private static final int BATCH_SIZE = 2;
    private static final List<String> UH_IDENTIFIERS = List.of("1", "2", "3", "4", "5");
    private static final List<List<String>> BATCHES = List.of(List.of("1", "2"), List.of("3", "4"), List.of("5"));
    private static final String GROUP_PATH = "group-path:include";

    // The real one, which wraps a command's failure in a GrouperException.
    private final ExecutorService exec = new ExecutorService();

    private java.util.concurrent.ExecutorService requestPool;

    private final List<List<String>> built = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger inFlight = new AtomicInteger();

    private final AtomicInteger mostInFlight = new AtomicInteger();

    private final List<Integer> batchesDone = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    public void setUp() {
        requestPool = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    public void tearDown() {
        requestPool.shutdownNow();
    }

    private BatchExecutor batchExecutor(int maxConcurrentRequests, Long... retryDelaysMillis) {
        return new BatchExecutor(exec, requestPool, BATCH_SIZE, BATCH_SIZE, maxConcurrentRequests,
                Arrays.asList(retryDelaysMillis));
    }

    @Test
    public void constructionRejectsSettingsBelowOneAndNegativeRetryDelays() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchExecutor(exec, requestPool, 0, 1, 1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new BatchExecutor(exec, requestPool, 1, 0, 1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new BatchExecutor(exec, requestPool, 1, 1, 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new BatchExecutor(exec, requestPool, 1, 1, 1, List.of(-1L)));
    }

    @Test
    public void sendsAListThatFitsInOneBatchAsOneRequest() {
        IdResults results = batchExecutor(4).lookup(BATCHES.get(0), commands(0), IdResults::merge, batchesDone::add);

        assertEquals(List.of(BATCHES.get(0)), built);
        assertEquals(BATCHES.get(0), results.ids);
        assertEquals(List.of(2), batchesDone);
    }

    @Test
    public void mergesTheResultsOfConcurrentBatchesInBatchOrder() {
        // The first batch is answered last.
        CountDownLatch laterBatchesDone = new CountDownLatch(2);
        Function<List<String>, Command<IdResults>> commands = batch -> {
            built.add(batch);
            return () -> {
                if (batch.equals(BATCHES.get(0))) {
                    await(laterBatchesDone);
                } else {
                    laterBatchesDone.countDown();
                }
                return new IdResults(batch);
            };
        };

        IdResults results = batchExecutor(4).lookup(UH_IDENTIFIERS, commands, IdResults::merge, batchesDone::add);

        assertEquals(UH_IDENTIFIERS, results.ids);
        assertEquals(List.of(1, 2, 2), batchesDone.stream().sorted().toList());
    }

    @Test
    public void sendsAtMostMaxConcurrentRequestsBatchesOfALookupOrRemovalAtOnce() {
        List<String> uhIdentifiers = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        BatchExecutor batchExecutor = batchExecutor(2);

        batchExecutor.lookup(uhIdentifiers, commands(30), IdResults::merge, batchesDone::add);
        assertEquals(2, mostInFlight.get());

        mostInFlight.set(0);
        batchExecutor.remove(uhIdentifiers, commands(30), IdResults::merge, batchesDone::add);
        assertEquals(2, mostInFlight.get());
    }

    @Test
    public void addsOneBatchAtATime() {
        IdResults results = batchExecutor(4).add(GROUP_PATH, UH_IDENTIFIERS, commands(20), IdResults::merge,
                batchesDone::add);

        assertEquals(1, mostInFlight.get());
        assertEquals(BATCHES, built);
        assertEquals(UH_IDENTIFIERS, results.ids);
        assertEquals(List.of(2, 2, 1), batchesDone);
    }

    @Test
    public void longAddsToTheSameGroupRunOneAfterTheOther() {
        assertEquals(1, mostInFlightOfTwoAdds(GROUP_PATH, GROUP_PATH));
    }

    @Test
    public void longAddsToDifferentGroupsRunAtTheSameTime() {
        assertEquals(2, mostInFlightOfTwoAdds(GROUP_PATH, "other-group-path:include"));
    }

    /**
     * Add UH_IDENTIFIERS to the two groups from two threads at once, and return the most add requests that were in
     * flight at once.
     */
    private int mostInFlightOfTwoAdds(String groupPath, String otherGroupPath) {
        BatchExecutor batchExecutor = batchExecutor(4);
        CountDownLatch bothStarted = new CountDownLatch(2);
        java.util.concurrent.ExecutorService callers = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<IdResults> first = CompletableFuture.supplyAsync(() -> batchExecutor.add(groupPath,
                    UH_IDENTIFIERS, waitingCommands(bothStarted), IdResults::merge, batchesDone::add), callers);
            CompletableFuture<IdResults> second = CompletableFuture.supplyAsync(() -> batchExecutor.add(otherGroupPath,
                    UH_IDENTIFIERS, waitingCommands(bothStarted), IdResults::merge, batchesDone::add), callers);

            assertEquals(UH_IDENTIFIERS, first.join().ids);
            assertEquals(UH_IDENTIFIERS, second.join().ids);
            return mostInFlight.get();
        } finally {
            callers.shutdownNow();
        }
    }

    @Test
    public void startsNoMoreBatchesOfAnAddAfterABatchFails() {
        Function<List<String>, Command<IdResults>> commands = batch -> {
            built.add(batch);
            return () -> {
                if (batch.equals(BATCHES.get(1))) {
                    throw new IllegalStateException("Grouper unavailable");
                }
                return new IdResults(batch);
            };
        };

        assertThrows(GrouperException.class,
                () -> batchExecutor(4).add(GROUP_PATH, UH_IDENTIFIERS, commands, IdResults::merge, batchesDone::add));
        assertEquals(BATCHES.subList(0, 2), built);
        assertEquals(List.of(2), batchesDone);
    }

    @Test
    public void startsNoMoreBatchesOfALookupAfterABatchFails() {
        // The first batch fails while the second is in flight: the second finishes, and the third is never started.
        CountDownLatch firstFailed = new CountDownLatch(1);
        Function<List<String>, Command<IdResults>> commands = batch -> {
            built.add(batch);
            return () -> {
                if (batch.equals(BATCHES.get(0))) {
                    firstFailed.countDown();
                    throw new IllegalStateException("Grouper unavailable");
                }
                await(firstFailed);
                sleep(200);
                return new IdResults(batch);
            };
        };

        GrouperException e = assertThrows(GrouperException.class,
                () -> batchExecutor(2).lookup(UH_IDENTIFIERS, commands, IdResults::merge, batchesDone::add));
        assertEquals("Grouper unavailable", e.getCause().getMessage());
        // The two batches in flight were built on two threads, in either order.
        assertEquals(Set.copyOf(BATCHES.subList(0, 2)), Set.copyOf(built));
        assertEquals(List.of(2), batchesDone);
    }

    @Test
    public void concurrentBatchesReleaseTheirPermitsWhenOnBatchDoneFails() {
        // Both permits are taken by the first two batches; the third can only start once one of them is released.
        IllegalStateException e = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(IllegalStateException.class,
                        () -> batchExecutor(2).lookup(UH_IDENTIFIERS, commands(0), IdResults::merge, count -> {
                            throw new IllegalStateException("progress failed");
                        })));
        assertEquals("progress failed", e.getMessage());
    }

    @Test
    public void sendsABatchAgainThatFailedWithoutAnAnswerFromGrouper() {
        IdResults results = batchExecutor(1, 0L, 0L).add(GROUP_PATH, BATCHES.get(0),
                failingCommands(1, () -> new RuntimeException("Problem in url", new SocketTimeoutException())),
                IdResults::merge, batchesDone::add);

        assertEquals(2, built.size());
        assertEquals(BATCHES.get(0), results.ids);
        assertEquals(List.of(2), batchesDone);
    }

    @Test
    public void sendsABatchAgainWhenGrouperDidNotRespond() {
        batchExecutor(1, 0L).lookup(BATCHES.get(0),
                failingCommands(1, () -> new RuntimeException("Web service did not even respond! https://grouper")),
                IdResults::merge, batchesDone::add);

        assertEquals(2, built.size());
    }

    @Test
    public void sendsABatchAgainWhenGrouperFailedSomeOfItsMembersWithInternalErrors() {
        // As Grouper answers concurrent adds to one group.
        batchExecutor(1, 0L).add(GROUP_PATH, BATCHES.get(0),
                failingCommands(1, () -> addFailure("SUCCESS", "EXCEPTION")), IdResults::merge, batchesDone::add);

        assertEquals(2, built.size());
    }

    @Test
    public void doesNotSendABatchAgainThatGrouperFailedForAnotherReason() {
        assertThrows(GrouperException.class, () -> batchExecutor(1, 0L, 0L).add(GROUP_PATH, BATCHES.get(0),
                failingCommands(3, () -> addFailure("SUCCESS", "EXCEPTION", "SUBJECT_NOT_FOUND")),
                IdResults::merge, batchesDone::add));
        assertEquals(1, built.size());

        built.clear();
        assertThrows(GrouperException.class, () -> batchExecutor(1, 0L, 0L).lookup(BATCHES.get(0),
                failingCommands(3, () -> new GrouperClientWsException(null, "Problem with request")),
                IdResults::merge, batchesDone::add));
        assertEquals(1, built.size());

        built.clear();
        assertThrows(GrouperException.class, () -> batchExecutor(1, 0L, 0L).lookup(BATCHES.get(0),
                failingCommands(3, () -> new IllegalStateException("a bug")), IdResults::merge, batchesDone::add));
        assertEquals(1, built.size());
    }

    @Test
    public void givesUpAfterTheLastRetryDelay() {
        RuntimeException failure = new RuntimeException("Problem in url", new IOException("Connection reset"));

        GrouperException e = assertThrows(GrouperException.class, () -> batchExecutor(1, 0L, 0L).add(GROUP_PATH,
                BATCHES.get(0), failingCommands(3, () -> failure), IdResults::merge, batchesDone::add));

        assertSame(failure, e.getCause());
        assertEquals(3, built.size());
        assertTrue(batchesDone.isEmpty());
    }

    @Test
    public void isRetryableOnlyForFailuresThatCanPass() {
        assertTrue(BatchExecutor.isRetryable(new GrouperException("failed", new IOException())));
        // How the Grouper client throws a socket timeout.
        assertTrue(BatchExecutor.isRetryable(new GrouperException("failed",
                new RuntimeException("Problem in url", new SocketTimeoutException("Read timed out")))));
        assertTrue(BatchExecutor.isRetryable(removeFailure("EXCEPTION", "SUCCESS_WASNT_IMMEDIATE")));
        assertFalse(BatchExecutor.isRetryable(removeFailure("SUCCESS")));
        assertFalse(BatchExecutor.isRetryable(new GcWebServiceError(null, "Bad response from web service")));
        // Grouper answered, so the I/O error behind its answer doesn't make the failure one that can pass.
        assertFalse(BatchExecutor.isRetryable(new GcWebServiceError(new WsAddMemberResults(), new IOException())));
        assertFalse(BatchExecutor.isRetryable(new GrouperException("failed")));
    }

    /**
     * Commands that each take millis to answer, recording how many are in flight at once.
     */
    private Function<List<String>, Command<IdResults>> commands(long millis) {
        return batch -> {
            built.add(batch);
            return () -> {
                mostInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
                sleep(millis);
                inFlight.decrementAndGet();
                return new IdResults(batch);
            };
        };
    }

    /**
     * Commands whose first answer waits, for at most a moment, until both adds have started one, recording how many
     * are in flight at once.
     */
    private Function<List<String>, Command<IdResults>> waitingCommands(CountDownLatch bothStarted) {
        return batch -> {
            built.add(batch);
            return () -> {
                mostInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
                bothStarted.countDown();
                await(bothStarted, 300);
                sleep(10);
                inFlight.decrementAndGet();
                return new IdResults(batch);
            };
        };
    }

    /**
     * Commands of which the first failures fail with the failure supplied, and the rest succeed.
     */
    private Function<List<String>, Command<IdResults>> failingCommands(int failures,
            java.util.function.Supplier<RuntimeException> failure) {
        return batch -> {
            built.add(batch);
            boolean fails = built.size() <= failures;
            return () -> {
                if (fails) {
                    throw failure.get();
                }
                return new IdResults(batch);
            };
        };
    }

    private static GcWebServiceError addFailure(String... memberResultCodes) {
        WsAddMemberResults response = new WsAddMemberResults();
        response.setResults(Arrays.stream(memberResultCodes).map(resultCode -> {
            WsAddMemberResult result = new WsAddMemberResult();
            result.setResultMetadata(resultMetadata(resultCode));
            return result;
        }).toArray(WsAddMemberResult[]::new));
        return new GcWebServiceError(response, "Bad response from web service: resultCode: PROBLEM_WITH_ASSIGNMENT");
    }

    private static GcWebServiceError removeFailure(String... memberResultCodes) {
        WsDeleteMemberResults response = new WsDeleteMemberResults();
        response.setResults(Arrays.stream(memberResultCodes).map(resultCode -> {
            WsDeleteMemberResult result = new WsDeleteMemberResult();
            result.setResultMetadata(resultMetadata(resultCode));
            return result;
        }).toArray(WsDeleteMemberResult[]::new));
        return new GcWebServiceError(response, "Bad response from web service: resultCode: PROBLEM_DELETING_MEMBERS");
    }

    private static WsResultMeta resultMetadata(String resultCode) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(resultCode);
        return resultMetadata;
    }

    private static void await(CountDownLatch latch) {
        await(latch, 5000);
    }

    private static void await(CountDownLatch latch, long millis) {
        try {
            latch.await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The identifiers a batch was sent for, as the answer to it.
     */
    private static class IdResults extends Results {
        private final List<String> ids;

        IdResults(List<String> ids) {
            this.ids = List.copyOf(ids);
        }

        static IdResults merge(List<IdResults> batchResults) {
            return new IdResults(batchResults.stream().flatMap(results -> results.ids.stream()).toList());
        }

        @Override
        public String getResultCode() {
            return "SUCCESS";
        }
    }
}
