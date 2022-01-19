package edu.hawaii.its.api.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminListsHolderTest {

    private AdminListsHolder adminListHolder;
    private AdminListsHolder adminListHolder2;

    @BeforeEach
    public void setup() {
        List<GroupingPath> groupingPaths = new ArrayList<>();
        List<Person> people = new ArrayList<>();
        groupingPaths.add(new GroupingPath("path:to:grouping"));
        people.add(new Person("admin"));
        adminListHolder = new AdminListsHolder();
        adminListHolder2 = new AdminListsHolder(groupingPaths, new Group(people));
    }

    @Test
    public void nullTest() {
        assertNotNull(adminListHolder);
        assertNotNull(adminListHolder2);
    }

    @Test
    public void setAllGroupingPathsTest() {
        List<GroupingPath> groupingPaths = new ArrayList<>();
        groupingPaths.add(new GroupingPath("path:to:grouping"));
        adminListHolder.setAllGroupingPaths(groupingPaths);
        assertNotNull(adminListHolder.allGroupingPaths);
        assertEquals("path:to:grouping", adminListHolder.allGroupingPaths.get(0).path);
    }

    @Test
    public void getAllGroupingPathsTest() {
        assertNotNull(adminListHolder2.getAllGroupingPaths());
        assertTrue(adminListHolder2.getAllGroupingPaths().size() > 0);
        assertEquals("path:to:grouping", adminListHolder2.getAllGroupingPaths().get(0).path);
    }

    @Test
    public void setAdminGroup() {
        List<Person> people = new ArrayList<>();
        people.add(new Person("admin"));
        adminListHolder.setAdminGroup(new Group(people));
        assertNotNull(adminListHolder.adminGroup);
        assertEquals("admin", adminListHolder.adminGroup.getMembers().get(0).getName());

    }

    @Test
    public void getAdminGroup() {
        assertNotNull(adminListHolder2.getAdminGroup());
        assertNotNull(adminListHolder2.getAdminGroup().getMembers());
        assertNotNull(adminListHolder2.getAdminGroup().getMembers().get(0));
        assertEquals("admin", adminListHolder2.getAdminGroup().getMembers().get(0).getName());
    }
}
