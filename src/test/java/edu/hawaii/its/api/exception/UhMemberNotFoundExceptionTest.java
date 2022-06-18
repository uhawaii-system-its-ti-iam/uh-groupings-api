package edu.hawaii.its.api.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UhMemberNotFoundExceptionTest {

    @Test
    public void construction() {
        UhMemberNotFoundException ex = new UhMemberNotFoundException("fail");
        assertNotNull(ex);
        assertThat(ex.getMessage(), equalTo("404 NOT_FOUND \"fail\""));
    }
}
