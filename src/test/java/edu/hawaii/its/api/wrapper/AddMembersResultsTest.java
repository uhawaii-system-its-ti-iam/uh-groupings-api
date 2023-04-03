package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AddMembersResultsTest {

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_NUMBERS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void construction() {
        String json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertNotNull(new AddMembersResults(null));
        assertNotNull(new AddMembersResults());
    }

    @Test
    public void test() {
        String json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        List<AddMemberResult> addMemberResults = addMembersResults.getResults();
        assertNotNull(addMemberResults);
        assertEquals(5, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals(TEST_NUMBERS.get(0), addMemberResult.getUhUuid());
        assertEquals(TEST_USERNAMES.get(0), addMemberResult.getUid());
        assertEquals(TEST_NAMES.get(0), addMemberResult.getName());
    }
}
