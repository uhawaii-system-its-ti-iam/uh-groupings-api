package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class InvalidUhIdentifierExceptionTest {

    @Test
    public void construction() {
        InvalidUhIdentifierException exception = new InvalidUhIdentifierException("fail");
        assertNotNull(exception);
        assertEquals("400 BAD_REQUEST \"fail\"", exception.getMessage());
    }

}
