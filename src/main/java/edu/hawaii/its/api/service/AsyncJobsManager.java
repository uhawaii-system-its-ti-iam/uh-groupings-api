package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AsyncJobResult;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AsyncJobsManager {

    private final ConcurrentMap<Integer, CompletableFuture<?>> jobMap;

    public AsyncJobsManager() {
        jobMap = new ConcurrentHashMap<>();
    }

    public void putJob(Integer jobId, CompletableFuture<?> job) {
        jobMap.put(jobId, job);
    }

    public CompletableFuture<?> getJob(Integer jobId) {
        return jobMap.get(jobId);
    }

    public void removeJob(Integer jobId) {
        jobMap.remove(jobId);
    }

    public AsyncJobResult getJobResult(Integer jobId) throws Exception {
        CompletableFuture<?> completableFuture = getJob(jobId);
        if (completableFuture == null) {
            return new AsyncJobResult(jobId, "NOT_FOUND");
        }
        if (!completableFuture.isDone()) {
            return new AsyncJobResult(jobId, "IN_PROGRESS");
        }
        removeJob(jobId);
        return new AsyncJobResult(jobId, "COMPLETED", completableFuture.get());
    }
}
