package edu.hawaii.its.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CredentialFailureExceptionTest {

    @Test
    public void construction() {
        CredentialFailureException exception = new CredentialFailureException("fail");
        assertNotNull(exception);
        assertEquals("fail", exception.getMessage());
    }

}
