package edu.hawaii.its.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AccessDeniedExceptionTest {

    @Test
    public void construction() {
        AccessDeniedException exception = new AccessDeniedException();
        assertNotNull(exception);
        assertEquals("Insufficient Privileges", exception.getMessage());

        exception = new AccessDeniedException("fail");
        assertNotNull(exception);
        assertEquals("fail", exception.getMessage());

        Throwable throwable = new Throwable();
        assertNotNull(throwable);
        assertNull(throwable.getMessage());
        exception = new AccessDeniedException(throwable);
        assertNotNull(exception);
        assertEquals(throwable.toString(), exception.getMessage());
        assertEquals("java.lang.Throwable", exception.getMessage());
        assertEquals(throwable, exception.getCause());

        throwable = new Throwable("fail");
        assertNotNull(throwable);
        assertEquals("fail", throwable.getMessage());
        exception = new AccessDeniedException(throwable);
        assertNotNull(exception);
        assertEquals(throwable.toString(), exception.getMessage());
        assertEquals("java.lang.Throwable: fail", exception.getMessage());
        assertEquals(throwable, exception.getCause());

        exception = new AccessDeniedException("fail", throwable);
        assertNotNull(exception);
        assertEquals("fail", exception.getMessage());
        assertEquals(throwable, exception.getCause());
    }
}
