package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Set;
>>>>>>> Add GenericServiceResultTest.java

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
<<<<<<< HEAD
=======
import static org.junit.Assert.assertTrue;
>>>>>>> Add GenericServiceResultTest.java

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
<<<<<<< HEAD
        assertNotNull(genericServiceResult);
        assertEquals(this.string, genericServiceResult.get("string"));
        assertEquals(this.integer, genericServiceResult.get("integer"));
        assertEquals(this.floats, genericServiceResult.get("floats"));
=======
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult("TEST", "adding single objects");

        assertNotNull(genericServiceResult);
        // Check what was added on setup.
        constructionHelper(genericServiceResult);

        genericServiceResult.add("groupingsServiceResult", groupingsServiceResult);
        assertEquals(groupingsServiceResult, genericServiceResult.get("groupingsServiceResult"));
        assertTrue(checkIndices(3, "groupingsServiceResult"));

        constructionHelper(genericServiceResult);

        genericServiceResult.add(Arrays.asList("this", "isTest"), genericServiceResult, true);
        assertEquals(genericServiceResult, genericServiceResult.get("this"));
        assertEquals(true, genericServiceResult.get("isTest"));
        assertTrue(checkIndices(4, "this"));
        assertTrue(checkIndices(5, "isTest"));
    }

    /**
     * Check if added objects are accessible by string value.
     */
    private void constructionHelper(GenericServiceResult genericServiceResult) {

        assertEquals(this.string, genericServiceResult.get("string"));
        assertEquals(this.integer, genericServiceResult.get("integer"));
        assertEquals(this.floats, genericServiceResult.get("floats"));

        assertTrue(checkIndices(0, "string"));
        assertTrue(checkIndices(1, "integer"));
        assertTrue(checkIndices(2, "floats"));
    }

    /**
     * Check if the set S of objects added, where S(0 = "1stObjectAdded, ... n = "lastObjectAdded").
     */
    private boolean checkIndices(int i, String key) {
        return i == genericServiceResult.getMap().get(key);
>>>>>>> Add GenericServiceResultTest.java
    }

    @Test
    public void addTest() {
<<<<<<< HEAD
        genericServiceResult.add("boolean", true);
        assertEquals(true, genericServiceResult.get("boolean"));
    }
    public void removeTest() {
        
=======
        this.genericServiceResult.add("boolean", true);
        assertEquals(true, this.genericServiceResult.get("boolean"));
    }

    public void removeTest() {

>>>>>>> Add GenericServiceResultTest.java
    }
}
