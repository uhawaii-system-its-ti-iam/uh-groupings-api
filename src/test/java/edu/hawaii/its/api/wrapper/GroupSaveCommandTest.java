package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GroupSaveCommandTest {
    @Test
    public void constructor() {
        GroupSaveCommand groupSaveCommand = new GroupSaveCommand();
        assertNotNull(groupSaveCommand);
    }

    @Test
    public void builders() {
        GroupSaveCommand groupSaveCommand = new GroupSaveCommand();
        assertNotNull(groupSaveCommand.setDescription(""));
        assertEquals(groupSaveCommand.self(), groupSaveCommand);
    }
}
