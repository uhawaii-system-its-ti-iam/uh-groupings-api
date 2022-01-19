package edu.hawaii.its.api.type;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.MembershipService;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMemberResult {
    @Autowired
    GrouperApiService grouperApiService;

    @Autowired MembershipService membershipService;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    private final String testUsername = "iamtst01";

    @BeforeAll
    public void init() {
        grouperApiService.addMember(GROUPING_INCLUDE, testUsername);
    }

    @Test
    public void constructorTest() {
        WsDeleteMemberResults deleteMemberResults = grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
        assertNotNull(deleteMemberResults);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(deleteMemberResults);
        assertNotNull(removeMemberResult);
        assertTrue(removeMemberResult.isUserWasRemoved());
        assertEquals("SUCCESS", removeMemberResult.getResult());
        assertEquals(testUsername, removeMemberResult.getUid());
        assertNotNull(removeMemberResult.getName());
        assertEquals(testUsername, removeMemberResult.getUhUuid());
        assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());

        deleteMemberResults = grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
        removeMemberResult = new RemoveMemberResult(deleteMemberResults);
        assertNotNull(removeMemberResult);
        assertFalse(removeMemberResult.isUserWasRemoved());
        assertEquals("FAILURE", removeMemberResult.getResult());
        assertEquals(testUsername, removeMemberResult.getUid());
        assertNotNull(removeMemberResult.getName());
        assertEquals(testUsername, removeMemberResult.getUhUuid());
        assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
    }
}
