package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AsyncJobResult;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAsyncJobsManager {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Autowired
    private AsyncJobsManager asyncJobsManager;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Test
    public void construction() {
        assertNotNull(asyncJobsManager);
    }

    @Test
    public void notFoundAsyncJobTest() {
        assertThrows(AccessDeniedException.class,
                () -> asyncJobsManager.getJobResult("bogus-owner-admin", 0));

        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(ADMIN, 0);
        assertEquals(0, asyncJobResult.getId());
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());

        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("SUCCESS"));
        asyncJobsManager.getJobResult(ADMIN, jobId);
        asyncJobResult = asyncJobsManager.getJobResult(ADMIN, jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());
    }

    @Test
    public void inProgressAsyncJobTest() {
        Integer jobId = asyncJobsManager.putJob(new CompletableFuture<>());
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(ADMIN, jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("IN_PROGRESS", asyncJobResult.getStatus());
        assertEquals("", asyncJobResult.getResult());
    }

    @Test
    public void completedAsyncJobTest() {
        String testUid = TEST_UIDS.get(0);
        updateMemberService.addOwnership(ADMIN, GROUPING, testUid);
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("SUCCESS");
        Integer jobId = asyncJobsManager.putJob(completableFuture);
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(testUid, jobId);
        assertEquals(jobId, asyncJobResult.getId());
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals(completableFuture.join(), asyncJobResult.getResult());
        updateMemberService.removeOwnership(ADMIN, GROUPING, testUid);
    }
}
