package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingOwnerMembersTest {

    private final Integer OWNER_LIMIT = 100;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void testConstructorWithOwnerLimit() {
        GroupingOwnerMembers GroupingOwnerMembers = new GroupingOwnerMembers(OWNER_LIMIT);
        assertNotNull(GroupingOwnerMembers);
        assertEquals("", GroupingOwnerMembers.getResultCode());
        assertEquals("", GroupingOwnerMembers.getGroupPath());
        assertEquals(new ArrayList<>(), GroupingOwnerMembers.getOwners().getMembers());
        assertEquals(0, GroupingOwnerMembers.getOwners().getSize());
        assertEquals(OWNER_LIMIT, GroupingOwnerMembers.getOwnerLimit());
    }

    @Test
    public void testConstructorWithGetMembersResults() {
        GetMembersResults getMembersResults =
                groupingsTestConfiguration.getMembersResultsSuccessTestData();
        GroupingOwnerMembers GroupingOwnerMembers = new GroupingOwnerMembers(getMembersResults.getMembersResults()
                .get(2), OWNER_LIMIT);
        assertNotNull(GroupingOwnerMembers);
        assertEquals("grouping-path:owners", GroupingOwnerMembers.getGroupPath());
        assertEquals("SUCCESS", GroupingOwnerMembers.getResultCode());
        assertEquals(OWNER_LIMIT, GroupingOwnerMembers.getOwnerLimit());
        List<GroupingGroupMember> results = GroupingOwnerMembers.getOwners().getMembers();
        assertNotNull(results);
        assertEquals(GroupingOwnerMembers.getOwners().getSize(), results.size());
    }

    @Test
    public void testConstructorWithSubjectsResults() {
        SubjectsResults subjectsResults =
                groupingsTestConfiguration.getSubjectsResultsSuccessTestData();
        GroupingOwnerMembers GroupingOwnerMembers = new GroupingOwnerMembers(subjectsResults, OWNER_LIMIT);
        assertNotNull(GroupingOwnerMembers);
        assertEquals("SUCCESS", GroupingOwnerMembers.getResultCode());
        assertEquals(OWNER_LIMIT, GroupingOwnerMembers.getOwnerLimit());
        List<GroupingGroupMember> results = GroupingOwnerMembers.getOwners().getMembers();
        assertNotNull(results);
        assertEquals(GroupingOwnerMembers.getOwners().getSize(), results.size());
    }
}
