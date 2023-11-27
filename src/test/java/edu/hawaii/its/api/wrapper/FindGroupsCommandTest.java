package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
        assertNotNull(findGroupsCommand.addPaths(new ArrayList<>()));
    }
}
