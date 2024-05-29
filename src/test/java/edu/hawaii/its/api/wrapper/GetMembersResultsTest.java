package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GetMembersResultsTest {
    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Value("${groupings.api.test.uh-first-names}")
    private List<String> TEST_FIRSTNAMES;

    @Value("${groupings.api.test.uh-last-names}")
    private List<String> TEST_LASTNAMES;

    private PropertyLocator propertyLocator;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void constructor() {
        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        assertNotNull(wsGetMembersResults);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertNotNull(getMembersResults);
        assertNotNull(new GetMembersResults());
        assertNotNull(new GetMembersResults(null));
    }

    @Test
    public void successfulResults() {
        String groupingPath = "grouping-path";

        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertEquals(SUCCESS, getMembersResults.getResultCode());

        List<GetMembersResult> membersResults = getMembersResults.getMembersResults();
        assertNotNull(membersResults);
        assertFalse(membersResults.isEmpty());

        GetMembersResult includeResult = membersResults.get(0);
        assertNotNull(includeResult);
        assertNotNull(includeResult.getGroup());
        assertEquals(groupingPath + ":include", includeResult.getGroup().getGroupPath());
        assertEquals(SUCCESS, includeResult.getResultCode());

        List<Subject> includeMembers = includeResult.getSubjects();
        assertNotNull(includeMembers);
        assertFalse(includeMembers.isEmpty());
        Subject subject = includeMembers.get(0);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(TEST_UIDS.get(0), subject.getUid());
        assertEquals(TEST_UH_UUIDS.get(0), subject.getUhUuid());
        assertEquals(TEST_NAMES.get(0), subject.getName());
        assertEquals(TEST_LASTNAMES.get(0), subject.getLastName());
        assertEquals(TEST_FIRSTNAMES.get(0), subject.getFirstName());

        subject = includeMembers.get(1);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(TEST_UIDS.get(1), subject.getUid());
        assertEquals(TEST_UH_UUIDS.get(1), subject.getUhUuid());
        assertEquals(TEST_NAMES.get(1), subject.getName());
        assertEquals(TEST_LASTNAMES.get(1), subject.getLastName());
        assertEquals(TEST_FIRSTNAMES.get(1), subject.getFirstName());

        GetMembersResult excludeResult = membersResults.get(1);
        assertNotNull(excludeResult);
        assertNotNull(excludeResult.getGroup());
        assertEquals(groupingPath + ":exclude", excludeResult.getGroup().getGroupPath());
        assertEquals(SUCCESS, excludeResult.getResultCode());

        List<Subject> excludeMembers = excludeResult.getSubjects();
        assertNotNull(excludeMembers);
        assertFalse(excludeMembers.isEmpty());
        subject = excludeMembers.get(0);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(TEST_UIDS.get(2), subject.getUid());
        assertEquals(TEST_UH_UUIDS.get(2), subject.getUhUuid());
        assertEquals(TEST_NAMES.get(2), subject.getName());
        assertEquals(TEST_LASTNAMES.get(2), subject.getLastName());
        assertEquals(TEST_FIRSTNAMES.get(2), subject.getFirstName());

        subject = excludeMembers.get(1);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(TEST_UIDS.get(3), subject.getUid());
        assertEquals(TEST_UH_UUIDS.get(3), subject.getUhUuid());
        assertEquals(TEST_NAMES.get(3), subject.getName());
        assertEquals(TEST_LASTNAMES.get(3), subject.getLastName());
        assertEquals(TEST_FIRSTNAMES.get(3), subject.getFirstName());

        GetMembersResult ownersResult = membersResults.get(2);
        assertNotNull(ownersResult);
        assertNotNull(ownersResult.getGroup());
        assertEquals(groupingPath + ":owners", ownersResult.getGroup().getGroupPath());
        assertEquals(SUCCESS, ownersResult.getResultCode());

        List<Subject> ownersMembers = ownersResult.getSubjects();
        assertNotNull(ownersMembers);
        assertFalse(ownersMembers.isEmpty());
        subject = ownersMembers.get(0);
        assertNotNull(subject);
        assertEquals(SUCCESS, subject.getResultCode());
        assertEquals(TEST_UIDS.get(4), subject.getUid());
        assertEquals(TEST_UH_UUIDS.get(4), subject.getUhUuid());
        assertEquals(TEST_NAMES.get(4), subject.getName());
        assertEquals(TEST_LASTNAMES.get(4), subject.getLastName());
        assertEquals(TEST_FIRSTNAMES.get(4), subject.getFirstName());
    }

    @Test
    public void noMembers() {
        String json = propertyLocator.find("ws.get.members.results.no.members");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        assertNotNull(wsGetMembersResults);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertNotNull(getMembersResults);
        assertEquals(SUCCESS, getMembersResults.getResultCode());
        List<GetMembersResult> membersResults = getMembersResults.getMembersResults();
        assertNotNull(membersResults);
        assertFalse(membersResults.isEmpty());
        GetMembersResult include = membersResults.get(0);
        assertNotNull(include);
        assertEquals(SUCCESS, include.getResultCode());
        assertNotNull(include.getGroup());
        assertTrue(include.getSubjects().isEmpty());
    }

    @Test
    public void multipleGroupsQueried() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        assertNotNull(wsGetMembersResults);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertNotNull(getMembersResults);
        List<GetMembersResult> groupsResult = getMembersResults.getMembersResults();
        /*
        assertEquals(2, groupsResult.size());
        assertEquals("group-path:exclude", groupsResult.get(0).getGroup().getGroupPath());
        assertEquals("group-path:include", groupsResult.get(1).getGroup().getGroupPath());

         */
    }

    @Test
    public void nullValues() {
        String json = propertyLocator.find("ws.get.members.results.null");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertEquals(SUCCESS, getMembersResults.getResultCode());
        assertEquals(FAILURE, getMembersResults.getMembersResults().get(0).getResultCode());
        assertEquals(FAILURE, getMembersResults.getMembersResults().get(0).getGroup().getResultCode());
        assertTrue(getMembersResults.getMembersResults().get(0).getSubjects().isEmpty());
        assertEquals(FAILURE, getMembersResults.getMembersResults().get(1).getResultCode());
        assertEquals(FAILURE, getMembersResults.getMembersResults().get(1).getGroup().getResultCode());
        assertTrue(getMembersResults.getMembersResults().get(1).getSubjects().isEmpty());
    }

    @Test
    public void emptyResults() {
        String json = propertyLocator.find("ws.empty.results");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        assertTrue(getMembersResults.getMembersResults().isEmpty());
        assertEquals(FAILURE, getMembersResults.getResultCode());
    }
}
