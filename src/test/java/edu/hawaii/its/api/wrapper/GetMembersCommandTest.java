package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
        assertNotNull(getMembersCommand.owner(""));
        assertNotNull(getMembersCommand.addSubjectAttribute(""));
        assertNotNull(getMembersCommand.assignMemberFilter(MemberFilter.ALL));
        assertEquals(getMembersCommand.self(), getMembersCommand);
    }
}
