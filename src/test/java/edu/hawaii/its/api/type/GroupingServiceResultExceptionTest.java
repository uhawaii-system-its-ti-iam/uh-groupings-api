package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import edu.hawaii.its.api.exception.GroupingsServiceResultException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingServiceResultExceptionTest extends GroupingsServiceResult {

    private GroupingsServiceResult groupingsServiceResult;
    private GroupingsServiceResultException groupingsServiceResultException;

    @BeforeEach
    public void setup() {
        groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResultException = new GroupingsServiceResultException();
    }

    @Test
    public void constructor() {
        assertNotNull(groupingsServiceResultException);
        assertNotNull(new GroupingsServiceResultException(groupingsServiceResult));
    }

    @Test
    public void accessors() {
        GroupingsServiceResultException exception = new GroupingsServiceResultException();
        exception.setGsr(groupingsServiceResult);
        assertEquals(groupingsServiceResult, exception.getGsr());
    }

}
