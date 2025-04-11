package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GroupAttributeCommandTest {
    @Test
    public void constructor() {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand();
        assertNotNull(groupAttributeCommand);
    }

    @Test
    public void builders() {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand();
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(groupAttributeCommand.addAttribute(""));
        assertNotNull(groupAttributeCommand.addAttributes(strings));
        assertNotNull(groupAttributeCommand.addGroup(""));
        assertNotNull(groupAttributeCommand.addGroups(strings));
        assertEquals(groupAttributeCommand.self(), groupAttributeCommand);
    }
}
