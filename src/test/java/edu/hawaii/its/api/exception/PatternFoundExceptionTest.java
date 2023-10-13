package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PatternFoundExceptionTest {

    @Test
    public void construction() {
        PatternFoundException exception = new PatternFoundException("fail");
        assertNotNull(exception);
        assertEquals("fail", exception.getMessage());
    }

}
