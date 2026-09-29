package edu.hawaii.its.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AsyncJobResult;

@Service
public class AsyncJobsManager {

    private final MemberService memberService;

    private static final Log logger = LogFactory.getLog(AsyncJobsManager.class);

    private final ConcurrentMap<Integer, CompletableFuture<?>> jobMap;

    public AsyncJobsManager(MemberService memberService) {
        jobMap = new ConcurrentHashMap<>();
        this.memberService = memberService;
    }

    public Integer putJob(CompletableFuture<?> job) {
        Integer jobId = job.hashCode();
        jobMap.put(jobId, job);
        return jobId;
    }

    public AsyncJobResult getJobResult(Integer jobId) {
        logger.debug(String.format("getJobResult; jobId: %s;", jobId));

        // Use JWT for general role checks instead of querying Grouper
        if (!memberService.isCurrentUserAdmin() && !memberService.isCurrentUserOwner()) {
            throw new AccessDeniedException();
        }

        CompletableFuture<?> job = jobMap.get(jobId);
        if (job == null) {
            return new AsyncJobResult(jobId, "NOT_FOUND");
        }
        if (!job.isDone()) {
            return new AsyncJobResult(jobId, "IN_PROGRESS");
        }

        jobMap.remove(jobId);
        try {
            return new AsyncJobResult(jobId, "COMPLETED", job.join());
        } catch (CompletionException e) {
            // join() wraps whatever the job threw. Rethrow the real failure so it maps to its own status
            // (e.g. 503 when Grouper is unavailable, 403 when access is denied) instead of a blanket 500.
            logger.warn(String.format("getJobResult; jobId: %s; job failed: %s", jobId, e.getCause()));
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
