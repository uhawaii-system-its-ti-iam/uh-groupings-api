package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GetMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsGroupMembersTest {

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        GroupingsGroupMembers groupingsGroupMembers = new GroupingsGroupMembers(getMembersResults.getMembersResults()
                .get(0));
        assertNotNull(groupingsGroupMembers);
        assertEquals("grouping-path:include", groupingsGroupMembers.getGroupPath());
        assertEquals("SUCCESS", groupingsGroupMembers.getResultCode());
        List<GroupingsGroupMember> results = groupingsGroupMembers.getGroupMembers();
        assertNotNull(results);
        assertEquals(2, results.size());

    }
}
