package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.MembershipResult;

public class MembershipResultsTest {

    @Test
    public void testMembershipResultsConstructor() {
        List<MembershipResult> memberships = new ArrayList<>();
        MembershipResult membershipResult = new MembershipResult();
        memberships.add(membershipResult);

        MembershipResults membershipResults = new MembershipResults(memberships);
        assertNotNull(membershipResults);
        assertEquals("SUCCESS", membershipResults.getResultCode());
        assertEquals(1, membershipResults.getResults().size());

        MembershipResults emptyResults = new MembershipResults();
        assertNotNull(emptyResults);
        assertEquals("FAILURE", emptyResults.getResultCode());
        assertEquals(0, emptyResults.getResults().size());
    }
}
