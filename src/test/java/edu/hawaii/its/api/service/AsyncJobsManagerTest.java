package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.type.AsyncJobProgress;
import edu.hawaii.its.api.type.AsyncJobResult;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AsyncJobsManagerTest {

    private static final Duration RETENTION = Duration.ofMinutes(10);

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private AsyncJobsManager asyncJobsManager;

    @BeforeEach
    public void beforeEach() {
        doReturn(true).when(memberService).isCurrentUserAdmin();
        doReturn(true).when(memberService).isCurrentUserOwner();
    }

    @Test
    public void getJobResultNotFoundTest() {
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(0);
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());

        doReturn(false).when(memberService).isCurrentUserAdmin();
        AsyncJobResult result = asyncJobsManager.getJobResult(0);
        assertEquals("NOT_FOUND", result.getStatus());
    }

    @Test
    public void getJobResultInProgressTest() {
        Integer jobId = asyncJobsManager.putJob(new CompletableFuture<>());
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals("IN_PROGRESS", asyncJobResult.getStatus());
    }

    @Test
    public void getJobResultReturnsTheProgressOfAJobInProgress() {
        AsyncJobProgress progress = new AsyncJobProgress();
        Integer jobId = asyncJobsManager.putJob(new CompletableFuture<>(), progress);
        // Before its first phase starts, a job has no progress to report.
        assertNull(asyncJobsManager.getJobResult(jobId).getProgress());

        progress.start(AsyncJobProgress.Phase.ADDING, 12284);
        progress.addDone(250);
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);

        assertEquals("IN_PROGRESS", asyncJobResult.getStatus());
        assertEquals(AsyncJobProgress.Phase.ADDING, asyncJobResult.getProgress().getPhase());
        assertEquals(250, asyncJobResult.getProgress().getDone());
        assertEquals(12284, asyncJobResult.getProgress().getTotal());
    }

    @Test
    public void getJobResultReturnsNoProgressWithTheResultOfAFinishedJob() {
        AsyncJobProgress progress = new AsyncJobProgress();
        progress.start(AsyncJobProgress.Phase.ADDING, 1);
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("completedJob"), progress);

        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);

        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertNull(asyncJobResult.getProgress());
    }

    @Test
    public void getJobResultCompletedTest() {
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("completedJob"));
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals("completedJob", asyncJobResult.getResult());
    }

    @Test
    public void getJobResultReturnsTheResultOfACompletedJobOnEveryRead() {
        // A poll sent again because the answer to the last one was lost gets the same result, not NOT_FOUND.
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("completedJob"));

        for (int read = 0; read < 3; read++) {
            AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
            assertEquals("COMPLETED", asyncJobResult.getStatus());
            assertEquals("completedJob", asyncJobResult.getResult());
        }
    }

    @Test
    public void getJobResultRethrowsTheRealFailureOfAFailedJob() {
        // An @Async method that throws completes its future with a CompletionException wrapping the failure.
        Integer grouperJobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new GrouperException("Grouper unavailable"))));
        assertThrows(GrouperException.class, () -> asyncJobsManager.getJobResult(grouperJobId));

        Integer deniedJobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new AccessDeniedException())));
        assertThrows(AccessDeniedException.class, () -> asyncJobsManager.getJobResult(deniedJobId));
    }

    @Test
    public void getJobResultRethrowsTheSameFailureOfAFailedJobOnEveryRead() {
        // The same exception maps to the same error status (503 for a GrouperException) every time.
        GrouperException failure = new GrouperException("Grouper unavailable");
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.failedFuture(new CompletionException(failure)));

        for (int read = 0; read < 3; read++) {
            assertSame(failure, assertThrows(GrouperException.class, () -> asyncJobsManager.getJobResult(jobId)));
        }
    }

    @Test
    public void evictFinishedJobsForgetsACompletedJobOnceItsRetentionHasPassed() {
        AtomicLong now = new AtomicLong();
        AsyncJobsManager manager = new AsyncJobsManager(memberService, now::get, RETENTION);
        Integer jobId = manager.putJob(CompletableFuture.completedFuture("completedJob"));
        assertEquals("COMPLETED", manager.getJobResult(jobId).getStatus());

        now.addAndGet(RETENTION.toNanos() - 1);
        manager.evictFinishedJobs();
        AsyncJobResult asyncJobResult = manager.getJobResult(jobId);
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals("completedJob", asyncJobResult.getResult());

        now.addAndGet(1);
        manager.evictFinishedJobs();
        assertEquals("NOT_FOUND", manager.getJobResult(jobId).getStatus());
        // Back inside the retention, a job that had only expired would be found again; the evicted one is gone.
        now.addAndGet(-1);
        assertEquals("NOT_FOUND", manager.getJobResult(jobId).getStatus());
    }

    @Test
    public void evictFinishedJobsForgetsAFailedJobThatWasNeverPolled() {
        AtomicLong now = new AtomicLong();
        AsyncJobsManager manager = new AsyncJobsManager(memberService, now::get, RETENTION);
        Integer jobId = manager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new GrouperException("Grouper unavailable"))));

        now.addAndGet(RETENTION.toNanos());
        manager.evictFinishedJobs();
        now.addAndGet(-1);

        assertEquals("NOT_FOUND", manager.getJobResult(jobId).getStatus());
    }

    @Test
    public void evictFinishedJobsKeepsAJobInProgressAndCountsTheRetentionFromWhenItFinishes() {
        AtomicLong now = new AtomicLong();
        AsyncJobsManager manager = new AsyncJobsManager(memberService, now::get, RETENTION);
        CompletableFuture<String> job = new CompletableFuture<>();
        Integer jobId = manager.putJob(job, new AsyncJobProgress());

        // An import can run for longer than the retention.
        now.addAndGet(RETENTION.toNanos() * 3);
        manager.evictFinishedJobs();
        assertEquals("IN_PROGRESS", manager.getJobResult(jobId).getStatus());

        job.complete("completedJob");
        now.addAndGet(RETENTION.toNanos() - 1);
        manager.evictFinishedJobs();
        assertEquals("COMPLETED", manager.getJobResult(jobId).getStatus());

        // Past its retention, a job is not found even before the next eviction has run.
        now.addAndGet(1);
        assertEquals("NOT_FOUND", manager.getJobResult(jobId).getStatus());
    }

    @Test
    public void constructorRejectsARetentionThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new AsyncJobsManager(memberService, System::nanoTime, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new AsyncJobsManager(memberService, System::nanoTime, Duration.ofMillis(-1)));
    }

    @Test
    public void getJobResultKeepsTheWrapperWhenTheFailureIsNotARuntimeException() {
        Integer jobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new Exception("checked"))));
        CompletionException e =
                assertThrows(CompletionException.class, () -> asyncJobsManager.getJobResult(jobId));
        assertEquals("checked", e.getCause().getMessage());
    }

    @Test
    public void getJobResultDeniedTest() {
        doReturn(false).when(memberService).isCurrentUserAdmin();
        doReturn(false).when(memberService).isCurrentUserOwner();
        assertThrows(AccessDeniedException.class, () -> asyncJobsManager.getJobResult(0));
    }

}
