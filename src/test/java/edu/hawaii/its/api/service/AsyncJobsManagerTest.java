package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AsyncJobResult;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AsyncJobsManagerTest {

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

    @MockBean
    private MemberService memberService;

    @Autowired
    private AsyncJobsManager asyncJobsManager;

    @BeforeEach
    public void beforeEach() {
        doReturn(true).when(memberService).isAdmin(CURRENT_USER);
        doReturn(true).when(memberService).isOwner(CURRENT_USER);
    }

    @Test
    public void getJobResultNotFoundTest() {
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(CURRENT_USER, 0);
        assertEquals("NOT_FOUND", asyncJobResult.getStatus());

        doReturn(false).when(memberService).isAdmin(CURRENT_USER);
        AsyncJobResult result = asyncJobsManager.getJobResult(CURRENT_USER, 0);
        assertEquals("NOT_FOUND", result.getStatus());
    }

    @Test
    public void getJobResultInProgressTest() {
        Integer jobId = asyncJobsManager.putJob(new CompletableFuture<>());
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(CURRENT_USER, jobId);
        assertEquals("IN_PROGRESS", asyncJobResult.getStatus());
    }

    @Test
    public void getJobResultCompletedTest() {
        Integer jobId = asyncJobsManager.putJob(CompletableFuture.completedFuture("completedJob"));
        AsyncJobResult asyncJobResult = asyncJobsManager.getJobResult(CURRENT_USER, jobId);
        assertEquals("COMPLETED", asyncJobResult.getStatus());
        assertEquals("completedJob", asyncJobResult.getResult());
    }

    @Test
    public void getJobResultDeniedTest() {
        doReturn(false).when(memberService).isAdmin(CURRENT_USER);
        doReturn(false).when(memberService).isOwner(CURRENT_USER);
        assertThrows(AccessDeniedException.class, () -> asyncJobsManager.getJobResult(CURRENT_USER, 0));
    }

}
