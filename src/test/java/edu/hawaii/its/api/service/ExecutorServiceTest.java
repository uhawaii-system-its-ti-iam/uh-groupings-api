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
public class ExecutorServiceTest {

    private final boolean RETRY = true;
    private final boolean NO_RETRY = false;

    @Mock
    private Command<MockResults> mockCommand;

    @Autowired
    private ExecutorService exec;
    
    @Test
    public void successfulExecution() {
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(NO_RETRY, mockCommand);

        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(RETRY, mockCommand);
    }

    @Test
    public void unsuccessfulExecution() {
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulExecution(NO_RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        assertUnsuccessfulExecution(NO_RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(RuntimeException.class).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);

        doThrow(RuntimeException.class).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        assertUnsuccessfulExecution(RETRY, mockCommand);
    }

    @Test
    public void catchInterruptedException() {
        Thread thread = new Thread(() -> exec.delay(0));
        thread.start();
        thread.interrupt();
        assertNotNull(thread);
    }

    private void assertSuccessfulExecution(boolean retry, Command<MockResults> command) {
        MockResults result = exec.execute(retry, command);
        assertNotNull(result);
        assertEquals(result.getResultCode(), "SUCCESS");
    }

    private void assertUnsuccessfulExecution(boolean retry, Command<MockResults> command) {
        MockResults result = exec.execute(retry, command);
        assertTrue(result == null || !result.getResultCode().equals("SUCCESS"));
    }

}
