package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupPathTest {

    private GroupPath include;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = PATH_ROOT + ":include";

    @Before
    public void setUp() {
        include = new GroupPath(INCLUDE);
    }

    @Test
    public void construction() {
        assertNotNull(include);
    }

    @Test
    public void toStringTest() {
        String str = "path: " + include.path + "; " +
                "parentPath: " + include.parentPath + "; " +
                "name: " + include.name + ";";
        assertEquals(str, include.toString());
    }

    @Test
    public void getParentPathTest() {
        assertEquals(include.parentPath, PATH_ROOT);
    }

    @Test
    public void getNameTest() {
        assertEquals(include.name, "grouping");
    }

}