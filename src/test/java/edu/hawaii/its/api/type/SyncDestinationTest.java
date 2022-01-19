package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SyncDestinationTest {

    private SyncDestination destination;

    @BeforeEach
    public void setUp() {
        destination = new SyncDestination();
    }

    @Test
    public void construction() {
        assertNotNull(destination);
        assertEquals("", destination.getName());
        assertEquals("", destination.getDescription());
        assertEquals("", destination.getTooltip());
        assertFalse(destination.isSynced());

        destination = new SyncDestination("name", "description");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertFalse(destination.isSynced());
        assertEquals("", destination.getTooltip());

        SyncDestination syncDestination = new SyncDestination(null, null);
        assertEquals("", syncDestination.getName());
        assertEquals("", syncDestination.getDescription());
    }

    @Test
    public void accessors() {
        assertEquals("", destination.getName());
        assertEquals("", destination.getDescription());
        assertEquals("", destination.getTooltip());
        assertFalse(destination.isSynced());

        destination.setSynced(true);
        assertTrue(destination.isSynced());
        assertEquals("", destination.getTooltip());
        assertEquals("", destination.getName());
        assertEquals("", destination.getDescription());

        destination.setDescription("description");
        assertTrue(destination.isSynced());
        assertThat(destination.getDescription(), equalTo("description"));
        assertEquals("", destination.getName());
        assertEquals("", destination.getTooltip());

        destination.setName("name");
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.isSynced());
        assertEquals("", destination.getTooltip());

        destination.setTooltip("tooltip");
        assertThat(destination.getTooltip(), equalTo("tooltip"));
        assertThat(destination.getName(), equalTo("name"));
        assertThat(destination.getDescription(), equalTo("description"));
        assertTrue(destination.isSynced());
    }

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
                "SyncDestination[" + "name='" + name + '\'' + ", description='" + description + '\'' + ", tooltip='"
                        + tooltip + '\'' + ", synced=" + true + ", hidden=" + false + ']';
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
