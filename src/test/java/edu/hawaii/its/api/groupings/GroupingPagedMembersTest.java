package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GroupingPagedMembersTest {

    @Test
    public void constructor() {
        GroupingPagedMembers groupingPagedMembers = new GroupingPagedMembers();

        assertNotNull(groupingPagedMembers);
        assertNotNull(groupingPagedMembers.getMembers());
        assertTrue(groupingPagedMembers.getMembers().isEmpty());
        assertEquals(Integer.valueOf(1), groupingPagedMembers.getPageNumber());
        assertEquals(Integer.valueOf(0), groupingPagedMembers.getTotalCount());
    }

    @Test
    public void accessors() {
        GroupingPagedMembers groupingPagedMembers = new GroupingPagedMembers();

        List<GroupingMember> members = new ArrayList<>();

        groupingPagedMembers.setMembers(members);
        groupingPagedMembers.setPageNumber(3);
        groupingPagedMembers.setTotalCount(25);

        assertSame(members, groupingPagedMembers.getMembers());
        assertEquals(Integer.valueOf(3), groupingPagedMembers.getPageNumber());
        assertEquals(Integer.valueOf(25), groupingPagedMembers.getTotalCount());
    }

    @Test
    public void setMembersAllowsEmptyList() {
        GroupingPagedMembers groupingPagedMembers = new GroupingPagedMembers();

        List<GroupingMember> members = new ArrayList<>();
        groupingPagedMembers.setMembers(members);

        assertNotNull(groupingPagedMembers.getMembers());
        assertTrue(groupingPagedMembers.getMembers().isEmpty());
        assertSame(members, groupingPagedMembers.getMembers());
    }
}