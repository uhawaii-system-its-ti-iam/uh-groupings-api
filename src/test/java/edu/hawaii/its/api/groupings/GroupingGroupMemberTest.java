package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.GetMembersResults;

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

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        GetMembersResults getMembersResults =
                groupingsTestConfiguration.getMembersResultsSuccessTestData();
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
