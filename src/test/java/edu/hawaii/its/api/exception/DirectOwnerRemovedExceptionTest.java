package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DirectOwnerRemovedExceptionTest {

    @Test
    public void construction() {
        DirectOwnerRemovedException dore = new DirectOwnerRemovedException();
        assertNotNull(dore);
        assertEquals("At least one direct user owner is required.", dore.getMessage());
    }
}