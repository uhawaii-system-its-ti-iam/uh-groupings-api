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
        assertEquals(PATH_ROOT, include.parentPath);
    }

    @Test
    public void setParentPathTest() {
        GroupPath path = new GroupPath(INCLUDE);
        path.setParentPath("Parent");
        assertEquals("Parent", path.getParentPath());
    }

    @Test
    public void getNameTest() {
        assertEquals("grouping", include.name );
    }

    @Test
    public void setNameTest() {
        GroupPath path = new GroupPath(INCLUDE);
        path.setName("Name");
        assertEquals("Name", path.name);
    }

    @Test
    public void getPathTest() {
        assertEquals(INCLUDE, include.path);
    }

    @Test
    public void setPathTest() {
        GroupPath path = new GroupPath(INCLUDE);
        path.setPath("path");
        assertEquals("path", path.path);
    }

}