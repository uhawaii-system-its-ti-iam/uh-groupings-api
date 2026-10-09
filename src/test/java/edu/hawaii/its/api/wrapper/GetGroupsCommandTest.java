package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GetGroupsCommandTest {
    @Test
    public void constructor() {
        GetGroupsCommand getGroupsCommand = new GetGroupsCommand();
        assertNotNull(getGroupsCommand);

    }
    @Test
    public void builders() {
        GetGroupsCommand getGroupsCommand = new GetGroupsCommand();
        assertNotNull(getGroupsCommand.addUhIdentifier(""));
        assertNotNull(getGroupsCommand.addUhIdentifier("testiwta"));
        assertNotNull(getGroupsCommand.query(""));
        assertEquals(getGroupsCommand.self(), getGroupsCommand);
    }

    @Test
    public void addGroupPaths() {
        GetGroupsCommand getGroupsCommand = new GetGroupsCommand();
        assertNotNull(getGroupsCommand.addGroupPaths(Collections.emptyList()));
        assertNotNull(getGroupsCommand.addGroupPaths(List.of("grouping-1", "grouping-2")));
    }
}
