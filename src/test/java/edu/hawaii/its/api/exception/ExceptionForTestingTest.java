package edu.hawaii.its.api.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ExceptionForTestingTest {

    @Test
    public void construction() {
        ExceptionForTesting ex = new ExceptionForTesting("Test Exception");
        assertNotNull(ex);
        assertThat(ex.getMessage(), equalTo("Test Exception"));
    }
}
