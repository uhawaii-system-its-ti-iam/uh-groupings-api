package edu.hawaii.its.api.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.exception.GroupingsHTTPException;

public class GroupingsHTTPExceptionTest extends RuntimeException {

    /**
   * 
   */
    private static final long serialVersionUID = -5524447476145640165L;
    private GroupingsHTTPException groupingsHTTPexception;

    @BeforeEach
    public void setUp() {
        groupingsHTTPexception = new GroupingsHTTPException();
    }


    @Test
    public void GroupingsHTTPExceptionConstructionTest() {
        assertNotNull(groupingsHTTPexception);
        assertNull(groupingsHTTPexception.getLocalizedMessage());
        assertNull(groupingsHTTPexception.getMessage());
        assertNull(groupingsHTTPexception.getCause());
    }

    @Test
    public void GroupingsHTTPExceptionExpandedTest() {

        Exception e = new RuntimeException("Test Case");

        groupingsHTTPexception = new GroupingsHTTPException("Error", e, 1);
        assertThat(groupingsHTTPexception.getMessage(), equalTo("Error"));
        assertThat(groupingsHTTPexception.getCause(), equalTo(e));
        assertThat(groupingsHTTPexception.getStatusCode(), equalTo(1));

        Exception e1 = new RuntimeException("File not found");

        groupingsHTTPexception = new GroupingsHTTPException("Error 1", e1);
        assertThat(groupingsHTTPexception.getMessage(), equalTo("Error 1"));
        assertThat(groupingsHTTPexception.getCause(), equalTo(e1));
        assertNull(groupingsHTTPexception.getStatusCode());

        groupingsHTTPexception = new GroupingsHTTPException("Error 2");
        assertThat(groupingsHTTPexception.getMessage(), equalTo("Error 2"));
        assertNull(groupingsHTTPexception.getCause());
        assertNull(groupingsHTTPexception.getStatusCode());
    }
}