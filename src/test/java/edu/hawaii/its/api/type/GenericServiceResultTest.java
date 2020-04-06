package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GenericServiceResultTest {
    GenericServiceResult genericServiceResult;
    String string = "STRING";
    Integer integer = 300;
    List<Float> floats = Arrays.asList(1.0f, 1.1f, 1.2f);

    @Before
    public void setup() {
        genericServiceResult = new GenericServiceResult(Arrays.asList("string", "integer", "floats"),
                this.string, this.integer, this.floats);
    }

    @Test
    public void construction() {
        assertNotNull(genericServiceResult);
        assertEquals(this.string, genericServiceResult.get("string"));
        assertEquals(this.integer, genericServiceResult.get("integer"));
        assertEquals(this.floats, genericServiceResult.get("floats"));
    }

    @Test
    public void addTest() {
        genericServiceResult.add("boolean", true);
        assertEquals(true, genericServiceResult.get("boolean"));
    }
    public void removeTest() {
        
    }
}
