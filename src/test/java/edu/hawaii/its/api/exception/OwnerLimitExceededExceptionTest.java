package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OwnerLimitExceededExceptionTest {

    @Test
    public void construction() {
        OwnerLimitExceededException olee = new OwnerLimitExceededException();
        assertNotNull(olee);
        assertEquals("Exceeded limit of allowed owners for a grouping.", olee.getMessage());
    }
}
