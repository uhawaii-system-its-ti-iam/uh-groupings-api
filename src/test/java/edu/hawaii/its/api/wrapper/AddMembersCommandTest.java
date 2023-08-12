package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(addMembersCommand.owner(""));
        assertNotNull(addMembersCommand.includeUhMemberDetails(true));
        assertNotNull(addMembersCommand.replaceGroupMembers(true));
    }
}
