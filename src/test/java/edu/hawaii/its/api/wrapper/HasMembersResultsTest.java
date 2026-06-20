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
public class HasMembersResultsTest {

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
        HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();

        assertNotNull(hasMembersResults);
        assertNotNull(new HasMembersResults(null));
        assertNotNull(new HasMembersResults());
    }

    @Test
    public void successfulResults() {
        HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();

        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        assertEquals("group-path", hasMembersResults.getGroupPath());
        assertNotNull(hasMembersResults.getResults());

        int i = 0;
        for (HasMemberResult result : hasMembersResults.getResults()) {
            assertEquals("IS_MEMBER", result.getResultCode());
            assertEquals(TEST_UIDS.get(i), result.getUid());
            assertEquals(TEST_UH_UUIDS.get(i), result.getUhUuid());
            assertEquals(TEST_NAMES.get(i), result.getName());
            i++;
        }

        hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();

        i = 0;
        for (HasMemberResult result : hasMembersResults.getResults()) {
            assertEquals("IS_MEMBER", result.getResultCode());
            assertEquals(TEST_UIDS.get(i), result.getUid());
            assertEquals(TEST_UH_UUIDS.get(i), result.getUhUuid());
            assertEquals(TEST_NAMES.get(i), result.getName());
            i++;
        }
    }

    @Test
    public void notMemberResults() {
        HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsNotMembersTestData();

        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        assertEquals("group-path", hasMembersResults.getGroupPath());
        assertNotNull(hasMembersResults.getResults());
        assertNotNull(hasMembersResults.getResults().get(0));
        assertEquals("IS_NOT_MEMBER", hasMembersResults.getResults().get(0).getResultCode());
    }

    @Test
    public void failedResults() {
        HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsMembersFailureTestData();

        assertNotNull(hasMembersResults);
        assertEquals("FAILURE", hasMembersResults.getResultCode());
        assertEquals("", hasMembersResults.getGroupPath());
        assertNotNull(hasMembersResults.getResults());
    }

    @Test
    public void nullGroup() {
        HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsNullGroupTestData();

        assertNotNull(hasMembersResults);
        assertEquals("FAILURE", hasMembersResults.getGroup().getResultCode());
        assertEquals("", hasMembersResults.getGroupPath());
    }
	
	@Test
	public void getExistingMembersWithMembers() {
		HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsMembersUidTestData();
		assertNotNull(hasMembersResults);
		
		List<HasMemberResult> existingMembers = hasMembersResults.getExistingMembers();
		assertNotNull(existingMembers);
		assertEquals(5, existingMembers.size());
		
		for (HasMemberResult result : existingMembers) {
			assertEquals("IS_MEMBER", result.getResultCode());
		}
	}
	
	@Test
	public void getExistingMembersWithNoMembers() {
		HasMembersResults hasMembersResults =
                groupingsTestConfiguration.hasMemberResultsIsNotMembersUidTestData();
		assertNotNull(hasMembersResults);
		
		List<HasMemberResult> existingMembers = hasMembersResults.getExistingMembers();
		assertNotNull(existingMembers);
		assertEquals(0, existingMembers.size());
	}
	
	@Test
	public void getExistingMembersWithMixedResults() {
		HasMembersResults hasMembersResults =
				groupingsTestConfiguration.hasMemberResultsMixedTestData();
		assertNotNull(hasMembersResults);
		
		List<HasMemberResult> allResults = hasMembersResults.getResults();
		assertEquals(5, allResults.size());
		
		List<HasMemberResult> existingMembers = hasMembersResults.getExistingMembers();
		assertNotNull(existingMembers);
		assertEquals(3, existingMembers.size());
		
		for (HasMemberResult result : existingMembers) {
			assertEquals("IS_MEMBER", result.getResultCode());
		}
	}
	
}
