package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetMembersCommandTest {
    @Test
    public void constructor() {
        GetMembersCommand getMembersCommand = new GetMembersCommand();
        assertNotNull(getMembersCommand);
    }

    @Test
    public void builders() {
        GetMembersCommand getMembersCommand = new GetMembersCommand();
        List<String> strings = new ArrayList<>();
        strings.add("");
        assertNotNull(getMembersCommand.addGroupPath(""));
        assertNotNull(getMembersCommand.addGroupPaths(strings));
        assertNotNull(getMembersCommand.setAscending(true));
        assertNotNull(getMembersCommand.setPageNumber(1));
        assertNotNull(getMembersCommand.setPageSize(1));
        assertNotNull(getMembersCommand.sortBy(""));
    }
}
