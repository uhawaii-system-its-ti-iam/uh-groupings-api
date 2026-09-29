package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.type.AsyncJobResult;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AsyncJobsManagerTest {

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

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
    public void getJobResultCompletedTest() {
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("completedJob"));
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(jobId);
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals("completedJob", asyncJobResult.getResult());
    }

    @Test
    public void getJobResultRethrowsTheRealFailureOfAFailedJob() {
        // An @Async method that throws completes its future with a CompletionException wrapping the failure.
        Integer grouperJobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new GrouperException("Grouper unavailable"))));
        assertThrows(GrouperException.class, () -> asyncJobsManager.getJobResult(CURRENT_USER, grouperJobId));

        Integer deniedJobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new AccessDeniedException())));
        assertThrows(AccessDeniedException.class, () -> asyncJobsManager.getJobResult(CURRENT_USER, deniedJobId));
    }

    @Test
    public void getJobResultRethrowsAFailedJobOnlyOnce() {
        Integer jobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new GrouperException("Grouper unavailable"))));
        assertThrows(GrouperException.class, () -> asyncJobsManager.getJobResult(CURRENT_USER, jobId));
        assertEquals("NOT_FOUND", asyncJobsManager.getJobResult(CURRENT_USER, jobId).getStatus());
    }

    @Test
    public void getJobResultKeepsTheWrapperWhenTheFailureIsNotARuntimeException() {
        Integer jobId = asyncJobsManager.putJob(
                CompletableFuture.failedFuture(new CompletionException(new Exception("checked"))));
        CompletionException e =
                assertThrows(CompletionException.class, () -> asyncJobsManager.getJobResult(CURRENT_USER, jobId));
        assertEquals("checked", e.getCause().getMessage());
    }

    @Test
    public void getJobResultDeniedTest() {
        doReturn(false).when(memberService).isCurrentUserAdmin();
        doReturn(false).when(memberService).isCurrentUserOwner();
        assertThrows(AccessDeniedException.class, () -> asyncJobsManager.getJobResult(0));
    }

}
