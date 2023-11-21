package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {SpringBootWebApplication.class})
public class AsyncConfigTest {

    @Autowired
    private AsyncConfig asyncConfig;

    @Test
    public void construction() {
        assertNotNull(asyncConfig);
    }

    @Test
    public void getAsyncExecutorTest() {
        assertNotNull(asyncConfig.getAsyncExecutor());
    }
}
