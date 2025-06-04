package edu.hawaii.its.api.wrapper;

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

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class HasMembersResultsTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        assertNotNull(new HasMembersResults(null));
        assertNotNull(new HasMembersResults());
    }

    @Test
    public void successfulResults() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
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

        json = propertyLocator.find("ws.has.member.results.is.members.uid");
        wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        hasMembersResults = new HasMembersResults(wsHasMemberResults);

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
        String json = propertyLocator.find("ws.has.member.results.is.not.members");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        assertEquals("group-path", hasMembersResults.getGroupPath());
        assertNotNull(hasMembersResults.getResults());
        assertNotNull(hasMembersResults.getResults().get(0));
        assertEquals("IS_NOT_MEMBER", hasMembersResults.getResults().get(0).getResultCode());
    }

    @Test
    public void failedResults() {
        String json = propertyLocator.find("ws.has.member.results.is.members.failure");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        assertEquals("FAILURE", hasMembersResults.getResultCode());
        assertEquals("", hasMembersResults.getGroupPath());
        assertNotNull(hasMembersResults.getResults());
    }

    @Test
    public void nullGroup() {
        String json = propertyLocator.find("ws.has.member.results.null.group");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        assertEquals("FAILURE", hasMembersResults.getGroup().getResultCode());
        assertEquals("", hasMembersResults.getGroupPath());
    }
	
	@Test
	public void getExistingMembersWithMembers() {
		String json = propertyLocator.find("ws.has.member.results.is.members.uid");
		WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
		HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
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
		String json = propertyLocator.find("ws.has.member.results.is.not.members.uid");
		WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
		HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
		assertNotNull(hasMembersResults);
		
		List<HasMemberResult> existingMembers = hasMembersResults.getExistingMembers();
		assertNotNull(existingMembers);
		assertEquals(0, existingMembers.size());
	}
	
	@Test
	public void getExistingMembersWithMixedResults() {
		String jsonMembers = propertyLocator.find("ws.has.member.results.is.members.uid");
		String jsonNonMembers = propertyLocator.find("ws.has.member.results.is.not.members.uid");
		
		WsHasMemberResults wsMemberResults = JsonUtil.asObject(jsonMembers, WsHasMemberResults.class);
		WsHasMemberResults wsNonMemberResults = JsonUtil.asObject(jsonNonMembers, WsHasMemberResults.class);
		
		WsHasMemberResults wsMixedResults = new WsHasMemberResults();
		wsMixedResults.setResultMetadata(wsMemberResults.getResultMetadata());
		wsMixedResults.setWsGroup(wsMemberResults.getWsGroup());
		
		int totalResults = 5;
		edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult[] mixedResults =
				new edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult[totalResults];
		mixedResults[0] = wsMemberResults.getResults()[0];
		mixedResults[1] = wsMemberResults.getResults()[1];
		mixedResults[2] = wsNonMemberResults.getResults()[0];
		mixedResults[3] = wsMemberResults.getResults()[2];
		mixedResults[4] = wsNonMemberResults.getResults()[1];
		
		wsMixedResults.setResults(mixedResults);
		
		HasMembersResults hasMembersResults = new HasMembersResults(wsMixedResults);
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
