package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class HasMemberResultTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        assertNotNull(new HasMemberResult());
        assertNotNull(new HasMemberResult(null));
    }

    @Test
    public void nullSubject() {

        HasMemberResult hasMemberResult =
                groupingsTestConfiguration.hasMemberResultNullSubjectResultCodeTestData();

        assertEquals("", hasMemberResult.getName());
        assertEquals("", hasMemberResult.getFirstName());
        assertEquals("", hasMemberResult.getLastName());
        assertEquals("", hasMemberResult.getUid());
        assertEquals("", hasMemberResult.getUhUuid());
        assertEquals("", hasMemberResult.getResultCode());
        assertNotNull(hasMemberResult.getSubject());
    }

    @Test
    public void isMemberReturnsTrue() {

        HasMembersResults hasMembersResults =
                groupingsTestConfiguration
                        .hasMemberResultsIsMembersUidTestData();

        HasMemberResult hasMemberResult =
                hasMembersResults.getResults().get(0);

        assertNotNull(hasMemberResult);
        assertEquals("IS_MEMBER", hasMemberResult.getResultCode());
        assertTrue(hasMemberResult.isMember());
    }

    @Test
    public void isMemberReturnsFalse() {

        HasMembersResults hasMembersResults =
                groupingsTestConfiguration
                        .hasMemberResultsIsNotMembersUidTestData();

        HasMemberResult hasMemberResult =
                hasMembersResults.getResults().get(0);

        assertNotNull(hasMemberResult);
        assertEquals("IS_NOT_MEMBER", hasMemberResult.getResultCode());
        assertFalse(hasMemberResult.isMember());
    }
}
