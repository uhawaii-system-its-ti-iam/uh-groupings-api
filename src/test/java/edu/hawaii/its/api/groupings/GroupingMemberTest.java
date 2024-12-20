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
import edu.hawaii.its.api.wrapper.Subject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingMemberTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        Subject subject = groupingsTestConfiguration.subjectSuccessUidTestData();
        GroupingGroupMember groupingGroupMember = new GroupingGroupMember(subject);
        GroupingMember groupingMember = new GroupingMember(groupingGroupMember, "Include");
        assertNotNull(groupingMember);

        assertEquals(TEST_UIDS.get(0), groupingMember.getUid());
        assertEquals(TEST_UH_UUIDS.get(0), groupingMember.getUhUuid());
        assertEquals(TEST_NAMES.get(0), groupingMember.getName());
        assertEquals(TEST_NAMES.get(0).split(" ")[0], groupingMember.getFirstName());
        assertEquals(TEST_NAMES.get(0).split(" ")[1], groupingMember.getLastName());
        assertEquals("Include", groupingMember.getWhereListed());

        groupingMember = new GroupingMember();
        assertNotNull(groupingMember);
        assertNotNull(groupingMember.getName());
        assertNotNull(groupingMember.getUhUuid());
        assertNotNull(groupingMember.getUid());
        assertNotNull(groupingMember.getWhereListed());
    }

}
