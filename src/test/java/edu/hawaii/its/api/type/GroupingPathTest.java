package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupingPathTest {
    private GroupingPath path;

    private static final String PATH_ROOT = "path:to:grouping";

    @Before
    public void setUp() {
        path = new GroupingPath(PATH_ROOT);
    }

    @Test
    public void construction() {
        assertNotNull(path);
    }

    @Test
    public void toStringTest() {
        String str = "path: " + path.path + "; " +
                "name: " + path.name + ";";
        assertEquals(str, path.toString());
    }

    @Test
    public void getNameTest() {
        assertEquals("grouping", path.name );
    }

    @Test
    public void setNameTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setName("Name");
        assertEquals("Name", path.name);
    }

    @Test
    public void getPathTest() {
        assertEquals(PATH_ROOT, path.path);
    }

    @Test
    public void setPathTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setPath("path");
        assertEquals("path", path.path);
    }
}
