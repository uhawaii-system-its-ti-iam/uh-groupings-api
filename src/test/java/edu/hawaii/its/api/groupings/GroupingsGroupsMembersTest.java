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

public class GroupingsGroupsMembersTest {
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        assertNotNull(wsGetMembersResults);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertNotNull(getMembersResults);
        GroupingsGroupsMembers groupingsGroupsMembers = new GroupingsGroupsMembers(getMembersResults);
        List<GroupingsGroupMembers> groupingsGroupMembers = groupingsGroupsMembers.getGroupsMembersList();
        assertNotNull(groupingsGroupMembers);
        assertEquals(2, groupingsGroupMembers.size());
    }
}
