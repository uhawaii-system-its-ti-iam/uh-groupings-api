package edu.hawaii.its.api.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class AccessDeniedExceptionTest {

    @Test
    public void construction() {
        AccessDeniedException exception = new AccessDeniedException();
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("Insufficient Privileges"));

        exception = new AccessDeniedException("fail");
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("fail"));

        Throwable throwable = new Throwable();
        assertNotNull(throwable);
        assertNull(throwable.getMessage());
        exception = new AccessDeniedException(throwable);
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo(throwable.toString()));
        assertThat(exception.getMessage(), equalTo("java.lang.Throwable"));
        assertThat(exception.getCause(), equalTo(throwable));

        throwable = new Throwable("fail");
        assertNotNull(throwable);
        assertThat(throwable.getMessage(), equalTo("fail"));
        exception = new AccessDeniedException(throwable);
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo(throwable.toString()));
        assertThat(exception.getMessage(), equalTo("java.lang.Throwable: fail"));
        assertThat(exception.getCause(), equalTo(throwable));

        exception = new AccessDeniedException("fail", throwable);
        assertNotNull(exception);
        assertThat(exception.getMessage(), equalTo("fail"));
        assertThat(exception.getCause(), equalTo(throwable));
    }
}
