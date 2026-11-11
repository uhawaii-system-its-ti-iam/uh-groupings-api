package edu.hawaii.its.api.service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AsyncJobProgress;
import edu.hawaii.its.api.type.AsyncJobResult;

/**
 * Keeps the async jobs, so their callers can poll them by id. A finished job is kept for finishedJobRetention after it
 * finishes, whether or not it is polled, and every poll in that time gets the same answer: its result, or its failure.
 * A poll that is sent again because its answer was lost on the way back (the UI does that) therefore still gets the
 * job's outcome, instead of NOT_FOUND. evictFinishedJobs then forgets the job.
 */
@Service
public class AsyncJobsManager {

    private static final Log logger = LogFactory.getLog(AsyncJobsManager.class);

    private final MemberService memberService;

    private final ConcurrentMap<Integer, Job> jobs = new ConcurrentHashMap<>();

    // The current time in nanoseconds, from a clock that only moves forward (System.nanoTime), so a change to the
    // system time can't make a job expire early or late.
    private final LongSupplier nanoTime;

    private final long finishedJobRetentionNanos;

    @Autowired
    public AsyncJobsManager(MemberService memberService,
            @Value("${groupings.api.async.finished-job-retention-millis}") long finishedJobRetentionMillis) {
        this(memberService, System::nanoTime, Duration.ofMillis(finishedJobRetentionMillis));
    }

    /**
     * @param memberService        checks the role of the current user
     * @param nanoTime             the current time in nanoseconds, from a clock that only moves forward
     * @param finishedJobRetention how long a finished job is kept after it finishes
     */
    AsyncJobsManager(MemberService memberService, LongSupplier nanoTime, Duration finishedJobRetention) {
        if (finishedJobRetention.isNegative() || finishedJobRetention.isZero()) {
            throw new IllegalArgumentException(
                    "The time a finished async job is kept must be positive: " + finishedJobRetention);
        }
        this.memberService = memberService;
        this.nanoTime = nanoTime;
        this.finishedJobRetentionNanos = finishedJobRetention.toNanos();
    }

    public Integer putJob(CompletableFuture<?> job) {
        return putJob(job, null);
    }

    /**
     * Put a job that reports its progress, which getJobResult returns while the job is in progress. progress is null
     * for a job that reports none.
     */
    public Integer putJob(CompletableFuture<?> job, AsyncJobProgress progress) {
        Integer jobId = job.hashCode();
        Job tracked = new Job(job, progress);
        jobs.put(jobId, tracked);
        // Runs at once if the job has already finished.
        job.whenComplete((result, failure) -> tracked.finishedAt = nanoTime.getAsLong());
        return jobId;
    }

    /**
     * The status of the job jobId: IN_PROGRESS (with its progress, if it reports any), COMPLETED with its result, or
     * NOT_FOUND for a job that is unknown or has been forgotten. A job that failed throws its failure instead. A
     * finished job gives the same answer every time it is read, until it is forgotten.
     */
    public AsyncJobResult getJobResult(Integer jobId) {
        logger.debug(String.format("getJobResult; jobId: %s;", jobId));

        // Use JWT for general role checks instead of querying Grouper
        if (!memberService.isCurrentUserAdmin() && !memberService.isCurrentUserOwner()) {
            throw new AccessDeniedException();
        }

        Job job = jobs.get(jobId);
        // A job past its retention is answered as forgotten even before evictFinishedJobs removes it, so whether it
        // is found doesn't depend on when the eviction last ran.
        if (job == null || job.isExpired(nanoTime.getAsLong(), finishedJobRetentionNanos)) {
            return new AsyncJobResult(jobId, "NOT_FOUND");
        }
        if (!job.future.isDone()) {
            AsyncJobResult inProgress = new AsyncJobResult(jobId, "IN_PROGRESS");
            if (job.progress != null) {
                inProgress.setProgress(job.progress.snapshot());
            }
            return inProgress;
        }

        try {
            return new AsyncJobResult(jobId, "COMPLETED", job.future.join());
        } catch (CompletionException e) {
            // join() wraps whatever the job threw. Rethrow the real failure so it maps to its own status
            // (e.g. 503 when Grouper is unavailable, 403 when access is denied) instead of a blanket 500. Every read
            // rethrows the same exception, so it maps to the same status every time.
            logger.warn(String.format("getJobResult; jobId: %s; job failed: %s", jobId, e.getCause()));
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }

    /**
     * Forget the jobs that finished more than finishedJobRetention ago, including those that were never polled (e.g.
     * the user closed the page). A job still in progress is kept however long it runs.
     */
    @Scheduled(fixedDelayString = "${groupings.api.async.job-eviction-interval-millis}")
    public void evictFinishedJobs() {
        long now = nanoTime.getAsLong();
        // Removes an entry only if it still holds the job checked, never a job put under the same id meanwhile.
        jobs.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(now, finishedJobRetentionNanos);
            if (expired) {
                logger.debug(String.format("evictFinishedJobs; jobId: %s;", entry.getKey()));
            }
            return expired;
        });
    }

    /**
     * A job, the progress it reports (null if it reports none), and when it finished.
     */
    private static final class Job {
        private final CompletableFuture<?> future;
        private final AsyncJobProgress progress;
        // The nanoTime at which the job finished, or null while it runs.
        private volatile Long finishedAt;

        private Job(CompletableFuture<?> future, AsyncJobProgress progress) {
            this.future = future;
            this.progress = progress;
        }

        private boolean isExpired(long now, long retentionNanos) {
            Long finished = finishedAt;
            // Compared by difference, as nanoTime values may overflow.
            return finished != null && now - finished >= retentionNanos;
        }
    }
}
