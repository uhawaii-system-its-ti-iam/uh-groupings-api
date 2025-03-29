package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.wrapper.Command;
import edu.hawaii.its.api.wrapper.MockCommand;
import edu.hawaii.its.api.wrapper.MockResults;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ExecutorServiceTest {

    @Spy
    private MockCommand mockCommand;

    @Autowired
    private ExecutorService exec;

    @Test
    public void successfulExecutionWithRetry() {
        mockCommand.setRetry(true);

        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(mockCommand);

        doThrow(new RuntimeException()).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(mockCommand);

        doThrow(new RuntimeException()).when(mockCommand).execute();
        doThrow(new RuntimeException()).when(mockCommand).execute();
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(mockCommand);
    }

    @Test
    public void successfulExecutionWithoutRetry() {
        mockCommand.setRetry(false);

        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertSuccessfulExecution(mockCommand);
    }

    @Test
    public void unsuccessfulExecutionWithRetry() {
        mockCommand.setRetry(true);

        doThrow(new RuntimeException()).when(mockCommand).execute();
        assertThrows(GrouperException.class, () -> exec.execute(mockCommand));

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        MockResults result = exec.execute(mockCommand);
        assertNotNull(result);
        assertEquals("FAILURE", result.getResultCode());

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        doThrow(new RuntimeException()).when(mockCommand).execute();
        doThrow(new RuntimeException()).when(mockCommand).execute();
        assertThrows(GrouperException.class, () -> exec.execute(mockCommand));
    }

    @Test
    public void unsuccessfulExecutionWithoutRetry() {
        mockCommand.setRetry(false);

        doThrow(new RuntimeException()).when(mockCommand).execute();
        assertUnsuccessfulExecution(mockCommand);

        doReturn(new MockResults("FAILURE")).when(mockCommand).execute();
        MockResults result = exec.execute(mockCommand);
        assertNotNull(result);
        assertEquals("FAILURE", result.getResultCode());
    }

    @Test
    public void catchInterruptedException() {
        Thread thread = new Thread(() -> exec.delay(0));
        thread.start();
        thread.interrupt();
        assertNotNull(thread);
    }

    private void assertSuccessfulExecution(MockCommand command) {
        MockResults result = exec.execute(command);
        assertNotNull(result);
        assertEquals("SUCCESS", result.getResultCode());
    }

    private void assertUnsuccessfulExecution(Command<MockResults> command) {
        assertThrows(GrouperException.class, () -> exec.execute(command));
    }

}
