package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupingPathTest {
    private GroupingPath path;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String DESCRIPTION = "description";

    @BeforeEach
    public void setUp() {
        path = new GroupingPath(PATH_ROOT, DESCRIPTION);
    }

    @Test
    public void construction() {
        GroupingPath groupingPath = new GroupingPath();
        assertNotNull(path);
        assertNotNull(groupingPath);

        groupingPath = new GroupingPath(PATH_ROOT, "");
        assertNotNull(groupingPath);
    }

    @Test
    public void toStringTest() {
        String str = "path: " + path.getPath() + "; " +
                "name: " + path.getName() + "; " +
                "description: " + path.getDescription() + ";";
        assertEquals(str, path.toString());
    }

    @Test
    public void getNameTest() {
        assertEquals("grouping", path.getName());
    }

    @Test
    public void setNameTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setName("Name");
        assertEquals("Name", path.getName());
    }

    @Test
    public void getPathTest() {
        assertEquals(PATH_ROOT, path.getPath());
    }

    @Test
    public void setPathTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setPath("path");
        assertEquals("path", path.getPath());
    }

    @Test
    public void getDescriptionTest() {
        assertEquals(DESCRIPTION, path.getDescription());
    }

    @Test
    public void setDescriptionTest() {
        GroupingPath path = new GroupingPath(PATH_ROOT);
        path.setDescription("Description");
        assertEquals("Description", path.getDescription());
    }

}
