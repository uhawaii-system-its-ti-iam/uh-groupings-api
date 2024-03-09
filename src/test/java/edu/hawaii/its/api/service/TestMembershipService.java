package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.type.Membership;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMembershipService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_true_empty}")
    private String EMPTY_GROUPING;

    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private List<String> testUids;

    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));
        testUids = uhIdentifierGenerator.getRandomMembers(5).getUids();
        testUids.forEach(testUid -> {
            grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
            grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
        });
    }

    @Test
    public void membershipResultsTest() {
        grouperService.removeMember(ADMIN, GROUPING_BASIS, testUids.get(0));

        // Should throw an exception when a non-admin user attempts to fetch memberships of another member.
        try {
            membershipService.membershipResults(testUids.get(0), testUids.get(1));
            fail("Should throw an exception when a non-admin user attempts to fetch memberships of another member.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.membershipResults("bogus-admin", testUids.get(0));
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin and does not match uid.
        grouperService.addMember(ADMIN, GROUPING_ADMINS, testUids.get(0));
        try {
            membershipService.membershipResults(testUids.get(0), ADMIN);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does not match uid.");
        } catch (UhMemberNotFoundException e) {

        }
        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUids.get(0));

        // Should throw an exception if uid passed is bogus.
        try {
            membershipService.membershipResults(ADMIN, "bogus-user");
            fail("Should throw an exception if uid passed is bogus.");
        } catch (UhMemberNotFoundException e) {
            assertEquals("404 NOT_FOUND \"bogus-user\"", e.getMessage());
        }

        grouperService.addMember(ADMIN, GROUPING_BASIS, testUids.get(0));
    }

    @Test
    public void managePersonResultsTest() {
        List<Membership> memberships;

        // Should not be a member.
        memberships = membershipService.managePersonResults(ADMIN, testUids.get(0));
        assertTrue(memberships.stream()
                .noneMatch(membership -> membership.getPath().equals(GROUPING) && !membership.isInBasis()));

        // Should be a member after added.
        grouperService.addMember(ADMIN, GROUPING_OWNERS, testUids.get(0));
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        grouperService.addMember(ADMIN, GROUPING_EXCLUDE, testUids.get(0));
        grouperService.addMember(ADMIN, GROUPING_BASIS, testUids.get(0));
        memberships = membershipService.managePersonResults(ADMIN, testUids.get(0));
        Membership membership = memberships.stream()
                .filter(m -> m.getPath().equals(GROUPING)).findAny().orElse(null);
        assertNotNull(membership);
        assertEquals(GROUPING, membership.getPath());
        assertTrue(membership.isInExclude());
        assertTrue(membership.isInBasis());
        assertTrue(membership.isInInclude());
        assertTrue(membership.isInOwner());

        // Clean up.
        grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUids.get(0));
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUids.get(0));
        grouperService.removeMember(ADMIN, GROUPING_BASIS, testUids.get(0));

        // Should throw an exception when a non-admin user attempts to fetch memberships of another member.
        try {
            membershipService.managePersonResults(testUids.get(0), testUids.get(1));
            fail("Should throw an exception when a non-admin user attempts to fetch memberships of another member.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.managePersonResults("bogus-admin", testUids.get(0));
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user matches uid and is not an admin.
        try {
            membershipService.managePersonResults(testUids.get(0), testUids.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user matches uid and is not an admin.");
        }

        // Should not throw an exception if current user is an admin and does not match uid.
        grouperService.addMember(ADMIN, GROUPING_ADMINS, testUids.get(0));
        try {
            membershipService.managePersonResults(testUids.get(0), testUids.get(1));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does not match uid.");
        }
        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUids.get(0));

        // Should not throw an exception if current user is an admin and does match uid.
        grouperService.addMember(ADMIN, GROUPING_ADMINS, testUids.get(0));
        try {
            membershipService.managePersonResults(testUids.get(0), testUids.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does match uid.");
        }
        grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUids.get(0));

        // Should return and empty list if uid passed is bogus.
        memberships = membershipService.managePersonResults(ADMIN, "bogus-user");
        assertTrue(memberships.isEmpty());
    }

    @Test
    public void getNumberOfMembershipsTest() {
        List<String> testUidList = Arrays.asList(testUids.get(0));
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, testUidList);
        updateMemberService.removeExcludeMembers(ADMIN, EMPTY_GROUPING, testUidList);

        int results = membershipService.numberOfMemberships(ADMIN, testUidList.get(0));
        updateMemberService.addIncludeMembers(ADMIN, EMPTY_GROUPING, testUidList);
        assertTrue(membershipService.numberOfMemberships(ADMIN, testUidList.get(0)) > results);
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, testUidList);

        // Should have groups for user in include, basis, and not exclude only.
        testUidList = Arrays.asList(testUids.get(0));

        results = membershipService.numberOfMemberships(ADMIN, testUidList.get(0));
        updateMemberService.addIncludeMembers(ADMIN, EMPTY_GROUPING, testUidList);
        updateMemberService.addExcludeMembers(ADMIN, EMPTY_GROUPING, testUidList);

        assertEquals(membershipService.numberOfMemberships(ADMIN, testUidList.get(0)), results);
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, testUidList);
        updateMemberService.removeExcludeMembers(ADMIN, EMPTY_GROUPING, testUidList);
    }

    /**
     * Helper - getMembershipResultsTest()
     * Create a sublist which contains all memberships whose path contains the currentUsers uh username (ADMIN) and who is not in basis.
     */
    private List<Membership> getMembershipsForCurrentUser(List<Membership> memberships) {
        return memberships
                .stream().filter(membership -> membership.getPath().equals(GROUPING))
                .collect(Collectors.toList())
                .stream().filter(membership -> !membership.isInBasis()).collect(Collectors.toList());
    }

    /**
     * Helper - updateLastModifiedTimestampTest
     * Get a random LocalDateTime between start and end.
     */
    private static LocalDateTime getRandomLocalDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return LocalDateTime.of(
                getRandomNumberBetween(start.getYear(), end.getYear()),
                getRandomNumberBetween(start.getMonthValue(), end.getMonthValue()),
                getRandomNumberBetween(start.getDayOfMonth(), end.getDayOfMonth()),
                getRandomNumberBetween(start.getHour(), end.getHour()),
                getRandomNumberBetween(start.getMinute(), end.getMinute()));
    }

    /**
     * Helper - getRandomLocalDateTimeBetween
     * Get a random number between start and end.
     */
    private static int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
}
