package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.MockResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class RetryExecutorServiceTest {

    @Mock
    private Command<MockResults> mockCommand;

    @Autowired
    private RetryExecutorService retryExec;

    @Test
    public void successfulRetry() {
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulRetry(mockCommand);
    }

    @Test
    public void unsuccessfulRetry() {
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        assertUnsuccessfulRetry(mockCommand);
    }

    @Test
    public void catchInterruptedException() {
        Thread thread = new Thread(() -> retryExec.delay(0));
        thread.start();
        thread.interrupt();
        assertNotNull(thread);
    }

    private void assertSuccessfulRetry(Command<MockResults> command) {
        MockResults result = retryExec.execute(command);
        assertNotNull(result);
        assertEquals(result.getResultCode(), "SUCCESS");
    }

    private void assertUnsuccessfulRetry(Command<MockResults> command) {
        MockResults result = retryExec.execute(command);
        assertTrue(result == null || !result.getResultCode().equals("SUCCESS"));
    }

}
