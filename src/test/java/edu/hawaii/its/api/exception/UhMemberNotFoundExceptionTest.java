package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UhMemberNotFoundExceptionTest {

    @Test
    public void construction() {
        UhMemberNotFoundException exception = new UhMemberNotFoundException("fail");
        assertNotNull(exception);
        assertEquals("404 NOT_FOUND \"fail\"", exception.getMessage());
    }

}
