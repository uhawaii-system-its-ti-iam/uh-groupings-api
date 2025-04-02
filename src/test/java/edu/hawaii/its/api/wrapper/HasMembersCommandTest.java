package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class HasMembersCommandTest {

    @Test
    public void constructor() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand);
    }

    @Test
    public void builders() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand.owner(""));
        assertNotNull(hasMembersCommand.assignGroupPath(""));
        assertNotNull(hasMembersCommand.addUhIdentifier(""));
        assertNotNull(hasMembersCommand.addUhIdentifier("11111111"));
        assertEquals(hasMembersCommand.self(), hasMembersCommand);

        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(hasMembersCommand.addUhIdentifiers(strings));
    }

}
