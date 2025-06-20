package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GrouperExceptionTest {

    @Test
    public void testDefaultConstructor() {
        GrouperException exception = new GrouperException();
        assertEquals("An error occurred upstream from GrouperClient", exception.getMessage());
    }

    @Test
    public void testConstructorWithMessage() {
        GrouperException exception = new GrouperException("Custom error");
        assertEquals("Custom error", exception.getMessage());
    }

    @Test
    public void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        GrouperException exception = new GrouperException(cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        GrouperException exception = new GrouperException("Combined", cause);
        assertEquals("Combined", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
