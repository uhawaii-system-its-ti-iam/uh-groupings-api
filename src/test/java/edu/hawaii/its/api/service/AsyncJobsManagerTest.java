package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AsyncJobResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AsyncJobsManagerTest {

    @Autowired
    private AsyncJobsManager asyncJobsManager;

    @Test
    public void construction() {
        assertNotNull(asyncJobsManager);
    }

    @Test
    public void completedAsyncJobTest() throws Exception {
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("SUCCESS");
        Integer jobId = completableFuture.hashCode();
        asyncJobsManager.putJob(jobId, completableFuture);
        assertEquals(completableFuture, asyncJobsManager.getJob(jobId));

        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals(completableFuture.get(), asyncJobResult.getResult());
    }

    @Test
    public void inProgressAsyncJobTest() throws Exception {
        CompletableFuture<?> completableFuture = new CompletableFuture<>();
        Integer jobId = completableFuture.hashCode();
        asyncJobsManager.putJob(jobId, completableFuture);
        assertEquals(completableFuture, asyncJobsManager.getJob(jobId));

        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("IN_PROGRESS", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());
    }

    @Test
    public void notFoundAsyncJobTest() throws Exception {
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(0);
        assertEquals(0, asyncJobResult.getId());
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());

        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("SUCCESS");
        Integer jobId = completableFuture.hashCode();
        asyncJobsManager.putJob(jobId, completableFuture);
        asyncJobsManager.getJobResult(jobId);
        asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());
    }
}
