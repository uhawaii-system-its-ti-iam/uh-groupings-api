package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    }
}
