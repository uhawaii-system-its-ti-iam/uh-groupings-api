package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.Command;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ExecutorServiceTest {

    @Mock
    private Command<String> mockCommand;

    @Autowired
    private ExecutorService executorService;

    @Test
    public void executeTest() {
        doReturn("execution success").when(mockCommand).execute();
        assertNotNull(executorService.execute(mockCommand));
        assertEquals("execution success", executorService.execute(mockCommand));

        doThrow(new RuntimeException("execution error")).when(mockCommand).execute();
        assertNull(executorService.execute(mockCommand));
    }

}
