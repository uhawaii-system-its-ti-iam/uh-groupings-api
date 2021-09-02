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
        assertNull(destination.getIsSynced());

        destination = new SyncDestination("name", "description");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertNull(destination.getIsSynced());
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
        assertNull(destination.getIsSynced());

        destination.setIsSynced(true);
        assertTrue(destination.getIsSynced());
        assertNull(destination.getTooltip());
        assertNull(destination.getDescription());
        assertNull(destination.getName());

        destination.setDescription("description");
        assertTrue(destination.getIsSynced());
        assertThat(destination.getDescription(), equalTo("description"));
        assertNull(destination.getName());
        assertNull(destination.getTooltip());

        destination.setName("name");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.getIsSynced());
        assertNull(destination.getTooltip());

        destination.setTooltip("tooltip");
        assertThat(destination.getTooltip(), equalTo("tooltip"));
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.getIsSynced());
    }

    @Test
    public void parseKeyVal() {
        String desc = "this is a description";
        String descReg = "this is ${} description with regex characters";
        String replacer = "replaced";

        assertThat(destination.parseKeyVal(replacer, desc), equalTo("this is a description"));

        assertThat(destination.parseKeyVal(replacer, descReg),
                equalTo("this is replaced description with regex characters"));
    }

    @Test
    public void toStringTest() {
        String name = "name";
        String description = "description";
        String tooltip = "tooltip";

        SyncDestination syncDestination = new SyncDestination(name, description);
        syncDestination.setTooltip(tooltip);
        syncDestination.setIsSynced(true);
        syncDestination.setHidden(false);
        String expected =
                "SyncDestination{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", tooltip='"
                        + tooltip + '\'' + ", isSynced=" + true + '\'' + ", hidden=" + false + '}';
        assertEquals(expected, syncDestination.toString());
    }

    @Test
    public void getSetHiddenTest() {
        String name = "name";
        String description = "description";
        SyncDestination syncDestination = new SyncDestination(name, description);
        assertFalse(syncDestination.getHidden());
        assertNotNull(syncDestination.getHidden());
        syncDestination.setHidden(true);
        assertTrue(syncDestination.getHidden());
        syncDestination.setHidden(null);
        assertFalse(syncDestination.getHidden());
        syncDestination.setHidden(false);
        assertFalse(syncDestination.getHidden());
    }

}
