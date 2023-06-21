package edu.hawaii.its.api.service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AsyncJobResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AsyncJobsManager {

    @Autowired
    MemberService memberService;

    private static final Log logger = LogFactory.getLog(AsyncJobsManager.class);

    private final ConcurrentMap<Integer, CompletableFuture<?>> jobMap;

    public AsyncJobsManager() {
        jobMap = new ConcurrentHashMap<>();
    }

    public Integer putJob(CompletableFuture<?> job) {
        Integer jobId = job.hashCode();
        jobMap.put(jobId, job);
        return jobId;
    }

    public AsyncJobResult getJobResult(String currentUser, Integer jobId) {
        logger.debug(String.format("getJobResult; currentUser: %s; jobId: %s;", currentUser, jobId));

        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
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
        return new AsyncJobResult(jobId, "COMPLETED", job.join());
    }
}
