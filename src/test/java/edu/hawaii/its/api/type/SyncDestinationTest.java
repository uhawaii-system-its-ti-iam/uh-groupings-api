package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SyncDestinationTest {

    private SyncDestination destination;

    @Before
    public void setUp() {
        destination = new SyncDestination();
    }

    @Test
    public void construction() {
        assertNotNull(destination);
        assertNull(destination.getName());
        assertNull(destination.getDescription());
        assertNull(destination.getTooltip());
        assertNull(destination.isSynced());

        destination = new SyncDestination("name", "description");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertNull(destination.isSynced());
        assertNull(destination.getTooltip());

        SyncDestination syncDestination = new SyncDestination(null, null);
        assertEquals("", syncDestination.getName());
        assertEquals("", syncDestination.getDescription());
    }

    @Test
    public void accessors() {
        assertNull(destination.getName());
        assertNull(destination.getDescription());
        assertNull(destination.getTooltip());
        assertNull(destination.isSynced());

        destination.setSynced(true);
        assertTrue(destination.isSynced());
        assertNull(destination.getTooltip());
        assertNull(destination.getDescription());
        assertNull(destination.getName());

        destination.setDescription("description");
        assertTrue(destination.isSynced());
        assertThat(destination.getDescription(), equalTo("description"));
        assertNull(destination.getName());
        assertNull(destination.getTooltip());

        destination.setName("name");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.isSynced());
        assertNull(destination.getTooltip());

        destination.setTooltip("tooltip");
        assertThat(destination.getTooltip(), equalTo("tooltip"));
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.isSynced());
    }

    /*
    @Test
    public void parseKeyVal() {
        String desc = "this is a description";
        String descReg = "this is ${} description with regex characters";
        String replacer = "replaced";

        assertThat(destination.parseKeyVal(replacer, desc), equalTo("this is a description"));

        assertThat(destination.parseKeyVal(replacer, descReg),
                equalTo("this is replaced description with regex characters"));
    }
     */

    @Test
    public void toStringTest() {
        String name = "name";
        String description = "description";
        String tooltip = "tooltip";

        SyncDestination syncDestination = new SyncDestination(name, description);
        syncDestination.setTooltip(tooltip);
        syncDestination.setSynced(true);
        syncDestination.setHidden(false);
        String expected =
                "SyncDestination{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", tooltip='"
                        + tooltip + '\'' + ", synced=" + true + '\'' + ", hidden=" + false + '}';
        assertEquals(expected, syncDestination.toString());
    }

    @Test
    public void getSetHiddenTest() {
        String name = "name";
        String description = "description";
        SyncDestination syncDestination = new SyncDestination(name, description);
        assertFalse(syncDestination.isHidden());
        assertNotNull(syncDestination.isHidden());
        syncDestination.setHidden(true);
        assertTrue(syncDestination.isHidden());
        syncDestination.setHidden(null);
        assertFalse(syncDestination.isHidden());
        syncDestination.setHidden(false);
        assertFalse(syncDestination.isHidden());
    }

}
