package edu.hawaii.its.api.wrapper;

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

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AddMembersResultsTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);
        assertNotNull(new AddMembersResults(null));
        assertNotNull(new AddMembersResults());
    }

    @Test
    public void test() {
        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        List<AddMemberResult> addMemberResults = addMembersResults.getResults();
        assertNotNull(addMemberResults);
        assertEquals(5, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals(TEST_UH_UUIDS.get(0), addMemberResult.getUhUuid());
        assertEquals(TEST_UIDS.get(0), addMemberResult.getUid());
        assertEquals(TEST_NAMES.get(0), addMemberResult.getName());
    }
}
