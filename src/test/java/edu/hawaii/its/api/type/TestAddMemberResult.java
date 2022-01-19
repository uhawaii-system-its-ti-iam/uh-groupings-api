package edu.hawaii.its.api.type;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.MembershipService;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMemberResult {
    @Autowired
    GrouperApiService grouperApiService;

    @Autowired MembershipService membershipService;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    private final String testUsername = "iamtst01";

    @BeforeAll
    public void init() {
        grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
        grouperApiService.addMember(GROUPING_EXCLUDE, testUsername);
    }

    @AfterAll
    public void cleanUp() {
        grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
    }

    @Test
    public void constructorTest() {
        WsDeleteMemberResults deleteMemberResults = grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
        assertNotNull(deleteMemberResults);
        WsAddMemberResults addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, testUsername);
        assertNotNull(addMemberResults);
        AddMemberResult addMemberResult = new AddMemberResult(addMemberResults, deleteMemberResults);
        assertNotNull(addMemberResult);
        assertTrue(addMemberResult.isUserWasAdded());
        assertTrue(addMemberResult.isUserWasRemoved());
        assertNotNull(addMemberResult.getName());
        assertEquals(testUsername, addMemberResult.getUid());
        assertEquals(testUsername, addMemberResult.getUhUuid());
        assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfRemoved());
        assertEquals("SUCCESS", addMemberResult.getResult());

        addMemberResult = new AddMemberResult(addMemberResults);
        assertNotNull(addMemberResult);
        assertTrue(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());
        assertNotNull(addMemberResult.getName());
        assertEquals(testUsername, addMemberResult.getUid());
        assertEquals(testUsername, addMemberResult.getUhUuid());
        assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        assertNull(addMemberResult.getPathOfRemoved());
        assertEquals("SUCCESS", addMemberResult.getResult());

        addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, testUsername);
        addMemberResult = new AddMemberResult(addMemberResults);
        assertNotNull(addMemberResult);
        assertFalse(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());
        assertNotNull(addMemberResult.getName());
        assertEquals(testUsername, addMemberResult.getUid());
        assertEquals(testUsername, addMemberResult.getUhUuid());
        assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        assertNull(addMemberResult.getPathOfRemoved());
        assertEquals("FAILURE", addMemberResult.getResult());

    }
}
