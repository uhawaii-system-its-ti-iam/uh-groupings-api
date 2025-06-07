package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

public class GroupingOwnerMembersTest {

    private final Integer OWNER_LIMIT = 100;

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

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
        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
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
        String json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        GroupingOwnerMembers GroupingOwnerMembers = new GroupingOwnerMembers(subjectsResults, OWNER_LIMIT);
        assertNotNull(GroupingOwnerMembers);
        assertEquals("SUCCESS", GroupingOwnerMembers.getResultCode());
        assertEquals(OWNER_LIMIT, GroupingOwnerMembers.getOwnerLimit());
        List<GroupingGroupMember> results = GroupingOwnerMembers.getOwners().getMembers();
        assertNotNull(results);
        assertEquals(GroupingOwnerMembers.getOwners().getSize(), results.size());
    }
}
