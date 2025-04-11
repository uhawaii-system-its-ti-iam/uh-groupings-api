package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RemoveMembersCommandTest {
    @Test
    public void constructor() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand();
        assertNotNull(removeMembersCommand);
    }

    @Test
    public void builders() {
        RemoveMembersCommand removeMembersCommand = new RemoveMembersCommand();
        assertNotNull(removeMembersCommand.addUhIdentifier(""));
        assertNotNull(removeMembersCommand.addUhIdentifier("11111111"));
        assertNotNull(removeMembersCommand.assignGroupPath(""));
        assertNotNull(removeMembersCommand.includeUhMemberDetails(true));
        assertNotNull(removeMembersCommand.owner(""));
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(removeMembersCommand.addUhIdentifiers(strings));
        assertEquals(removeMembersCommand.self(), removeMembersCommand);
    }
}
