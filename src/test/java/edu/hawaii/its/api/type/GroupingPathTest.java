package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingPathTest {
    private GroupingPath path;

    private static final String PATH_ROOT = "path:to:grouping";

    @BeforeEach
    public void setUp() {
        path = new GroupingPath(PATH_ROOT);
    }

    @Test
    public void construction() {
        GroupingPath groupingPath = new GroupingPath();
        assertNotNull(path);
        assertNotNull(groupingPath);
    }

    @Test
    public void toStringTest() {
        String str = "path: " + path.path + "; " +
                "name: " + path.name + "; " +
                "description: " + path.description + ";";
        assertEquals(str, path.toString());
    }

    @Test
    public void getNameTest() {
        assertEquals("grouping", path.name);
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

    @Test
    public void getDescriptionTest() {
        assertEquals("No description given for this Grouping.", path.description);
    }

    @Test
    public void setDescriptionTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setDescription("Description");
        assertEquals("Description", path.description);
    }

}
