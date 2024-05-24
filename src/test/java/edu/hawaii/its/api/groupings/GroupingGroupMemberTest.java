package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.GetMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingGroupMemberTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Value("${groupings.api.test.uh-first-names}")
    private List<String> TEST_FIRST_NAMES;

    @Value("${groupings.api.test.uh-last-names}")
    private List<String> TEST_LAST_NAMES;

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
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResults.getMembersResults()
                .get(0));
        assertNotNull(groupingGroupMembers);
        GroupingGroupMember groupingGroupMember = groupingGroupMembers.getMembers().get(0);
        assertNotNull(groupingGroupMember);
        assertEquals(TEST_UIDS.get(0), groupingGroupMember.getUid());
        assertEquals(TEST_NAMES.get(0), groupingGroupMember.getName());
        assertEquals(TEST_UH_UUIDS.get(0), groupingGroupMember.getUhUuid());
        assertEquals(TEST_FIRST_NAMES.get(0), groupingGroupMember.getFirstName());
        assertEquals(TEST_LAST_NAMES.get(0), groupingGroupMember.getLastName());
    }
}
