package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Mock
    private Command<MockResults> mockCommand;

    @Autowired
    private ExecutorService executorService;

    @Test
    public void executeTest() {
        doReturn(new MockResults("SUCCESS")).when(mockCommand).execute();
        assertNotNull(executorService.execute(mockCommand));
        assertEquals("SUCCESS", executorService.execute(mockCommand).getResultCode());

        doThrow(new RuntimeException()).when(mockCommand).execute();
        assertNull(executorService.execute(mockCommand));
    }

}
