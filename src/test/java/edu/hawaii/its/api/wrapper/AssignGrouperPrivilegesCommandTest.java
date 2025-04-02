package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AssignGrouperPrivilegesCommandTest {
    @Test
    public void constructor() {
        AssignGrouperPrivilegesCommand assignGrouperPrivilegesCommand = new AssignGrouperPrivilegesCommand();
        assertNotNull(assignGrouperPrivilegesCommand);
    }

    @Test
    public void builders() {
        AssignGrouperPrivilegesCommand assignGrouperPrivilegesCommand = new AssignGrouperPrivilegesCommand();
        assertNotNull(assignGrouperPrivilegesCommand.setGroupPath(""));
        assertNotNull(assignGrouperPrivilegesCommand.setPrivilege(""));
        assertNotNull(assignGrouperPrivilegesCommand.setSubjectLookup(""));
        assertNotNull(assignGrouperPrivilegesCommand.owner(""));
        assertNotNull(assignGrouperPrivilegesCommand.setIsAllowed(true));
        assertNotNull(assignGrouperPrivilegesCommand.setIsAllowed(false));
        assertEquals(assignGrouperPrivilegesCommand.self(), assignGrouperPrivilegesCommand);
    }

}
