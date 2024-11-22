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
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingAddResultTest {

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
        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);

        AddMemberResult addMemberResult = addMembersResults.getResults().get(0);
        GroupingAddResult groupingAddResult = new GroupingAddResult(addMemberResult);
        assertNotNull(groupingAddResult);
        assertEquals("SUCCESS_ALREADY_EXISTED", groupingAddResult.getResultCode());
        assertEquals(TEST_UIDS.get(0), groupingAddResult.getUid());
        assertEquals(TEST_UH_UUIDS.get(0), groupingAddResult.getUhUuid());
        assertEquals(TEST_NAMES.get(0), groupingAddResult.getName());

        addMemberResult = addMembersResults.getResults().get(2);
        groupingAddResult = new GroupingAddResult(addMemberResult);
        assertEquals("SUCCESS", groupingAddResult.getResultCode());
    }

}
