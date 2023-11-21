package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class JavaVersionExceptionTest {

    @Test
    public void construction() {
        JavaVersionException exception = new JavaVersionException("fail");
        assertNotNull(exception);
        assertEquals("fail", exception.getMessage());
    }

}
