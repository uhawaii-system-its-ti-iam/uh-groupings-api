package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class GroupPathNotFoundExceptionTest {

    @Test
    public void construction() {
        GroupPathNotFoundException exception = new GroupPathNotFoundException("fail");
        assertNotNull(exception);
        assertEquals("404 NOT_FOUND \"fail\"", exception.getMessage());
    }

}