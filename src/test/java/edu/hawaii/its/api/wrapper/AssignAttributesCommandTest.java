package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssignAttributesCommandTest {

    @Test
    public void constructor() {
        AssignAttributesCommand assignAttributesCommand = new AssignAttributesCommand();
        assertNotNull(assignAttributesCommand);
    }

    @Test
    public void builders() {
        AssignAttributesCommand assignAttributesCommand = new AssignAttributesCommand();
        assertNotNull(assignAttributesCommand.setAssignType(""));
        assertNotNull(assignAttributesCommand.setAssignOperation(""));
        assertNotNull(assignAttributesCommand.addGroupPath(""));
        assertNotNull(assignAttributesCommand.addAttribute(""));
        assertNotNull(assignAttributesCommand.owner(""));
        assertNotNull(assignAttributesCommand.setValueOperation(""));
        assertNotNull(assignAttributesCommand.addValue(""));
    }
}
