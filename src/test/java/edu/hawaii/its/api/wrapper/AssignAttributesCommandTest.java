package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
        assertEquals(assignAttributesCommand.self(), assignAttributesCommand);
    }
}
