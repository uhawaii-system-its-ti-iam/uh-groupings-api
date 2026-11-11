package edu.hawaii.its.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.IntConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.Results;

import edu.internet2.middleware.grouperClient.GrouperClientWsException;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

/**
 * Sends a command for a list of UH identifiers to Grouper as requests of at most a batch size of identifiers each,
 * and merges their results, so the caller gets the same result it would from a single request.
 * <p>
 * Grouper answers a request only after it has processed every subject in it, so one request for a long list (e.g. a
 * 10,000-row import) outlasts the Grouper client's socket timeout (grouperClient.webService.httpSocketTimeoutMillis).
 * <p>
 * Lookups and removals send up to maxConcurrentRequests batches at once, on a pool of request threads shared by all
 * of them. Adds send one batch at a time: Grouper fails most of the members of concurrent adds to one group (it
 * conflicts updating the group's last_membership_change), so two adds of more than one batch to the same group also
 * wait for each other.
 * <p>
 * A batch that fails without an answer from Grouper (e.g. a dropped connection), or for which Grouper reports only
 * internal errors (EXCEPTION) for its failed members, is sent again after each of the retry delays. Any other failure,
 * such as an unknown group, would fail the same way again. Sending a batch again is safe: a lookup only reads,
 * re-adding a listed member succeeds without changing it (SUCCESS_ALREADY_EXISTED), and so does removing one who is
 * not listed (SUCCESS_WASNT_IMMEDIATE).
 * <p>
 * The batches are not one transaction: once a batch has failed, no more batches are started and the failure is
 * rethrown, but the batches that succeeded stay applied in Grouper. The whole operation can be repeated.
 */
public class BatchExecutor {

    private static final Log logger = LogFactory.getLog(BatchExecutor.class);

    // The grouperClient message for a response that did not come from Grouper (e.g. a gateway error page).
    private static final String NO_RESPONSE_MESSAGE = "Web service did not even respond";

    private final ExecutorService exec;

    private final Executor requestPool;

    private final int lookupBatchSize;

    private final int updateBatchSize;

    private final int maxConcurrentRequests;

    private final List<Long> retryDelaysMillis;

    // One lock per group that has had an add of more than one batch. Locks are kept rather than removed when idle
    // (removing one safely would need a count of its users): there is at most one per Include or Exclude group.
    private final ConcurrentMap<String, ReentrantLock> addLocks = new ConcurrentHashMap<>();

    /**
     * @param exec                  executes each request
     * @param requestPool           runs the concurrent batches of lookups and removals
     * @param lookupBatchSize       the most identifiers in one lookup request
     * @param updateBatchSize       the most identifiers in one add or remove request
     * @param maxConcurrentRequests the most batches of one lookup or removal that are sent at once
     * @param retryDelaysMillis     how long to wait before each time a failed batch is sent again
     */
    public BatchExecutor(ExecutorService exec, Executor requestPool, int lookupBatchSize, int updateBatchSize,
            int maxConcurrentRequests, List<Long> retryDelaysMillis) {
        requireAtLeastOne("lookup batch size", lookupBatchSize);
        requireAtLeastOne("update batch size", updateBatchSize);
        requireAtLeastOne("number of concurrent requests", maxConcurrentRequests);
        if (retryDelaysMillis.stream().anyMatch(delay -> delay == null || delay < 0)) {
            throw new IllegalArgumentException("The Grouper retry delays must not be negative: " + retryDelaysMillis);
        }
        this.exec = exec;
        this.requestPool = requestPool;
        this.lookupBatchSize = lookupBatchSize;
        this.updateBatchSize = updateBatchSize;
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.retryDelaysMillis = List.copyOf(retryDelaysMillis);
    }

    /**
     * Look the uhIdentifiers up in batches, up to maxConcurrentRequests at once. onBatchDone is called with the
     * number of identifiers in each batch Grouper has answered.
     */
    public <T extends Results> T lookup(List<String> uhIdentifiers, Function<List<String>, Command<T>> commandForBatch,
            Function<List<T>, T> merge, IntConsumer onBatchDone) {
        return execute(uhIdentifiers, lookupBatchSize, maxConcurrentRequests, commandForBatch, merge, onBatchDone);
    }

    /**
     * Add the uhIdentifiers to the group at groupPath in batches, one at a time. An add of more than one batch first
     * waits for any other such add to the same group to finish.
     */
    public <T extends Results> T add(String groupPath, List<String> uhIdentifiers,
            Function<List<String>, Command<T>> commandForBatch, Function<List<T>, T> merge, IntConsumer onBatchDone) {
        if (uhIdentifiers.size() <= updateBatchSize) {
            return execute(uhIdentifiers, updateBatchSize, 1, commandForBatch, merge, onBatchDone);
        }
        ReentrantLock lock = addLocks.computeIfAbsent(groupPath, path -> new ReentrantLock(true));
        if (!lock.tryLock()) {
            logger.info(String.format("add; waiting for another add to %s to finish;", groupPath));
            lock.lock();
        }
        try {
            return execute(uhIdentifiers, updateBatchSize, 1, commandForBatch, merge, onBatchDone);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the uhIdentifiers in batches, up to maxConcurrentRequests at once.
     */
    public <T extends Results> T remove(List<String> uhIdentifiers, Function<List<String>, Command<T>> commandForBatch,
            Function<List<T>, T> merge, IntConsumer onBatchDone) {
        return execute(uhIdentifiers, updateBatchSize, maxConcurrentRequests, commandForBatch, merge, onBatchDone);
    }

    private <T extends Results> T execute(List<String> uhIdentifiers, int batchSize, int concurrency,
            Function<List<String>, Command<T>> commandForBatch, Function<List<T>, T> merge, IntConsumer onBatchDone) {
        int size = uhIdentifiers.size();
        if (size <= batchSize) {
            T result = executeBatch(uhIdentifiers, 1, 1, commandForBatch);
            onBatchDone.accept(size);
            return result;
        }
        List<List<String>> batches = new ArrayList<>();
        for (int from = 0; from < size; from += batchSize) {
            batches.add(uhIdentifiers.subList(from, Math.min(from + batchSize, size)));
        }
        List<T> results = new ArrayList<>(batches.size());
        RuntimeException failure = concurrency == 1
                ? executeOneAtATime(batches, commandForBatch, onBatchDone, results)
                : executeConcurrently(batches, concurrency, commandForBatch, onBatchDone, results);
        if (failure != null) {
            logger.error(String.format("execute; %d of %d batches had succeeded when a batch failed;",
                    results.size(), batches.size()));
            throw failure;
        }
        return merge.apply(results);
    }

    /**
     * Execute the batches in order, in this thread, collecting their results in results, up to the first failure,
     * which is returned.
     */
    private <T extends Results> RuntimeException executeOneAtATime(List<List<String>> batches,
            Function<List<String>, Command<T>> commandForBatch, IntConsumer onBatchDone, List<T> results) {
        for (int i = 0; i < batches.size(); i++) {
            List<String> batch = batches.get(i);
            try {
                results.add(executeBatch(batch, i + 1, batches.size(), commandForBatch));
            } catch (RuntimeException e) {
                return e;
            }
            onBatchDone.accept(batch.size());
        }
        return null;
    }

    /**
     * Execute the batches on the request pool, at most concurrency at once, collecting their results in results in
     * batch order. Once a batch has failed, no more are started; the batches already started are waited for, and
     * the first failure is returned.
     */
    private <T extends Results> RuntimeException executeConcurrently(List<List<String>> batches, int concurrency,
            Function<List<String>, Command<T>> commandForBatch, IntConsumer onBatchDone, List<T> results) {
        Semaphore permits = new Semaphore(concurrency);
        AtomicBoolean failed = new AtomicBoolean();
        List<CompletableFuture<T>> started = new ArrayList<>(batches.size());
        for (int i = 0; i < batches.size(); i++) {
            permits.acquireUninterruptibly();
            if (failed.get()) {
                permits.release();
                break;
            }
            List<String> batch = batches.get(i);
            int number = i + 1;
            started.add(CompletableFuture
                    .supplyAsync(() -> executeBatch(batch, number, batches.size(), commandForBatch), requestPool)
                    .whenComplete((result, e) -> {
                        // The permit is released even if onBatchDone throws, or the loop above would wait for it
                        // forever.
                        try {
                            if (e == null) {
                                onBatchDone.accept(batch.size());
                            } else {
                                failed.set(true);
                            }
                        } finally {
                            permits.release();
                        }
                    }));
        }
        RuntimeException failure = null;
        for (CompletableFuture<T> future : started) {
            try {
                results.add(future.join());
            } catch (CompletionException e) {
                if (failure == null) {
                    failure = e.getCause() instanceof RuntimeException cause ? cause : e;
                }
            }
        }
        return failure;
    }

    /**
     * Execute the command for one batch, sending it again after each retry delay while it fails in a way that can
     * pass (see isRetryable).
     */
    private <T extends Results> T executeBatch(List<String> batch, int number, int count,
            Function<List<String>, Command<T>> commandForBatch) {
        for (int attempt = 1; ; attempt++) {
            Command<T> command = commandForBatch.apply(batch);
            String text = String.format("executeBatch; command: %s; batch: %d of %d; identifiers: %d;",
                    command.getClass().getSimpleName(), number, count, batch.size());
            long start = System.nanoTime();
            try {
                T result = exec.execute(command);
                if (count > 1 || attempt > 1) {
                    logger.info(String.format("%s millis: %d;", text, (System.nanoTime() - start) / 1_000_000));
                }
                return result;
            } catch (RuntimeException e) {
                if (attempt > retryDelaysMillis.size() || !isRetryable(e)) {
                    logger.error(String.format("%s failed after %d attempt(s);", text, attempt));
                    throw e;
                }
                long delay = retryDelaysMillis.get(attempt - 1);
                logger.warn(String.format("%s attempt %d failed (%s); retrying in %d ms;", text, attempt,
                        e.getCause() == null ? e : e.getCause(), delay));
                pause(delay, e);
            }
        }
    }

    /**
     * Whether a request that failed with failure can succeed if it is sent again: it failed without an answer from
     * Grouper, or Grouper's answer reports only internal errors (EXCEPTION) for the members it failed. Grouper reports
     * those, for example, for the members of concurrent adds to one group.
     */
    static boolean isRetryable(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof GcWebServiceError error) {
                return failedOnlyWithInternalErrors(error.getContainerResponseObject());
            }
            if (cause instanceof GrouperClientWsException) {
                return false;
            }
        }
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof IOException || String.valueOf(cause.getMessage()).startsWith(NO_RESPONSE_MESSAGE)) {
                return true;
            }
        }
        return false;
    }

    private static boolean failedOnlyWithInternalErrors(Object response) {
        ResultMetadataHolder[] results = null;
        if (response instanceof WsAddMemberResults wsAddMemberResults) {
            results = wsAddMemberResults.getResults();
        } else if (response instanceof WsDeleteMemberResults wsDeleteMemberResults) {
            results = wsDeleteMemberResults.getResults();
        }
        if (results == null) {
            return false;
        }
        boolean failed = false;
        for (ResultMetadataHolder result : results) {
            WsResultMeta resultMetadata = result.getResultMetadata();
            String resultCode = resultMetadata == null ? null : resultMetadata.getResultCode();
            if (resultCode != null && resultCode.startsWith("SUCCESS")) {
                continue;
            }
            if (!"EXCEPTION".equals(resultCode)) {
                return false;
            }
            failed = true;
        }
        return failed;
    }

    private static void pause(long millis, RuntimeException failure) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw failure;
        }
    }

    private static void requireAtLeastOne(String name, int value) {
        if (value < 1) {
            throw new IllegalArgumentException("The Grouper " + name + " must be at least 1, but is " + value);
        }
    }
}
