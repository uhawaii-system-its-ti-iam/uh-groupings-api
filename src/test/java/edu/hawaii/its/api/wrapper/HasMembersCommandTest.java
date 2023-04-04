package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HasMembersCommandTest {

    @Test
    public void constructor() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand);
    }

    @Test
    public void builders() {
        HasMembersCommand hasMembersCommand = new HasMembersCommand();
        assertNotNull(hasMembersCommand.assignGroupPath(""));
        assertNotNull(hasMembersCommand.addUhIdentifier(""));
        assertNotNull(hasMembersCommand.addUhIdentifier("11111111"));

        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(hasMembersCommand.addUhIdentifiers(strings));
    }

}
