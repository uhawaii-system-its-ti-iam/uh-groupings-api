package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AddMembersCommandTest {
    @Test
    public void constructor() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        assertNotNull(addMembersCommand);
    }

    @Test
    public void builders() {
        AddMembersCommand addMembersCommand = new AddMembersCommand();
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(addMembersCommand.getGcAddMember());
        assertNotNull(addMembersCommand.addUhIdentifiers(strings));
        assertNotNull(addMembersCommand.addUhIdentifier(""));
        assertNotNull(addMembersCommand.addUhIdentifier("11111111"));
        assertNotNull(addMembersCommand.assignGroupPath(""));
        assertNotNull(addMembersCommand.addGroupPathOwner("test-group-path"));
        assertNotNull(addMembersCommand.addGroupPathOwners(strings));
        assertNotNull(addMembersCommand.owner(""));
        assertNotNull(addMembersCommand.includeUhMemberDetails(true));
        assertNotNull(addMembersCommand.replaceGroupMembers(true));
        assertEquals(addMembersCommand.self(), addMembersCommand);
    }
}
