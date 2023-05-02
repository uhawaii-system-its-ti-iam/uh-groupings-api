package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(assignGrouperPrivilegesCommand.setIsAllowed(true));
        assertNotNull(assignGrouperPrivilegesCommand.setIsAllowed(false));
    }

}
