package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupingSyncDestinationTest {

    private GroupingSyncDestination dest;

    @BeforeEach
    public void beforeEach() {
        dest = new GroupingSyncDestination();
    }

    @Test
    public void constructor() {
        assertNotNull(dest);
    }

    @Test
    public void accessors() {
        dest.setDescription("description");
        dest.setName("name");
        dest.setHidden(false);
        dest.setSynced(true);
        dest.setTooltip("tooltip");

        assertEquals("description", dest.getDescription());
        assertEquals("name", dest.getName());
        assertEquals(false, dest.getHidden());
        assertEquals(true, dest.getSynced());
        assertEquals("tooltip", dest.getTooltip());
    }

}
