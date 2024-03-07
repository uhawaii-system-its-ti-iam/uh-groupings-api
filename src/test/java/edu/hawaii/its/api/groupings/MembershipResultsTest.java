package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

public class MembershipResultsTest {

    @Test
    public void testMembershipResultsConstructor() {
        List<Membership> memberships = new ArrayList<>();
        Group group = new Group("tmp:testiwta:testiwta-aux");
        Person person = new Person("testiwta-name", "testiwta-uuid", "testiwta-username");
        group.addMember(person);
        Membership membership = new Membership(person, group);
        memberships.add(membership);

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
