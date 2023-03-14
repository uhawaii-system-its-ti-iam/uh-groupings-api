package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HasMemberResultTest {

    @Test
    public void construction() {
        assertNotNull(new HasMemberResult());
        assertNotNull(new HasMemberResult(null));
    }
}
