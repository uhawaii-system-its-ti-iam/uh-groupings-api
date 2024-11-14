package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

public class GroupingGroupMembersTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void testConstructorWithGetMembersResults() {
        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResults.getMembersResults()
                .get(0));
        assertNotNull(groupingGroupMembers);
        assertEquals("grouping-path:include", groupingGroupMembers.getGroupPath());
        assertEquals("SUCCESS", groupingGroupMembers.getResultCode());
        List<GroupingGroupMember> results = groupingGroupMembers.getMembers();
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void testConstructorWithSubjectsResults() {
        String json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(subjectsResults);
        assertNotNull(groupingGroupMembers);
        assertEquals("SUCCESS", groupingGroupMembers.getResultCode());
        List<GroupingGroupMember> results = groupingGroupMembers.getMembers();
        assertNotNull(results);
        assertEquals(4, results.size());
    }
}
