package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PasswordFoundExceptionTest {

    @Test
    public void construction() {
        PasswordFoundException exception = new PasswordFoundException("location");
        assertNotNull(exception);
        assertEquals("location", exception.getMessage());
    }

}
