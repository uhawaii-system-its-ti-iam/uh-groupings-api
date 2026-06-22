package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    public void construction() {
        assertNotNull(asyncConfig);
    }

    @Test
    public void getAsyncExecutorTest() {
        assertNotNull(asyncConfig.getAsyncExecutor());
    }
}
