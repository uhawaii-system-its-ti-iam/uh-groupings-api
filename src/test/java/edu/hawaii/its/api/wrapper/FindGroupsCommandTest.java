package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindGroupsCommandTest {
    @Test
    public void constructor() {
        FindGroupsCommand findGroupsCommand = new FindGroupsCommand();
        assertNotNull(findGroupsCommand);
    }

    @Test
    public void builders() {
        FindGroupsCommand findGroupsCommand = new FindGroupsCommand();
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(findGroupsCommand.addPath(""));
        assertNotNull(findGroupsCommand.addPaths(strings));
    }
}
