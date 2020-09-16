package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupPathTest {

    private GroupPath include;
    /*
    private GroupPath exclude;
    private GroupPath owners;
    private GroupPath basis;
     */

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String INCLUDE = PATH_ROOT + ":include";
    /*
    private static final String EXCLUDE = PATH_ROOT + ":exclude";
    private static final String OWNERS = PATH_ROOT + ":owners";
    private static final String BASIS = PATH_ROOT + ":basis";
     */

    @Before
    public void setUp() {
        include = new GroupPath(INCLUDE);
    }

    @Test
    public void construction() {
        assertNotNull(include);
        assertEquals(include.parentPath, PATH_ROOT);
        assertEquals(include.name, "grouping");
    }

    @Test
    public void toStringTest() {
        String str = "path: " + include.path + "; " +
                "parentPath: " + include.parentPath + "; " +
                "name: " + include.name + ";";
        assertEquals(str, include.toString());
    }
}
