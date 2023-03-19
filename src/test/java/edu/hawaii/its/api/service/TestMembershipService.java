package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    public Environment env;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    private final String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";

    private Person testPerson;

    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUsername);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_OWNERS, testUsername);
        });
        TEST_UH_NUMBERS.forEach(testNumber -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testNumber);
            grouperApiService.removeMember(GROUPING_INCLUDE, testNumber);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testNumber);
            grouperApiService.removeMember(GROUPING_OWNERS, testNumber);
        });
        testPerson = uhIdentifierGenerator.getRandomPerson();
        grouperApiService.removeMember(GROUPING_ADMINS, testPerson.getUsername());
        grouperApiService.removeMember(GROUPING_INCLUDE, testPerson.getUsername());
        grouperApiService.removeMember(GROUPING_EXCLUDE, testPerson.getUsername());
        grouperApiService.removeMember(GROUPING_OWNERS, testPerson.getUsername());

        grouperApiService.removeMember(GROUPING_ADMINS, testPerson.getUhUuid());
        grouperApiService.removeMember(GROUPING_INCLUDE, testPerson.getUhUuid());
        grouperApiService.removeMember(GROUPING_EXCLUDE, testPerson.getUhUuid());
        grouperApiService.removeMember(GROUPING_OWNERS, testPerson.getUhUuid());
    }

    @Test
    public void membershipResultsTest() {
        String testUsername = testPerson.getUsername();

        grouperApiService.removeMember(GROUPING_BASIS, testUsername);

        // Should throw an exception when a non-admin user attempts to fetch memberships of another member.
        try {
            membershipService.membershipResults(testUsername, TEST_USERNAMES.get(1));
            fail("Should throw an exception when a non-admin user attempts to fetch memberships of another member.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.membershipResults("bogus-admin", testUsername);
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user matches uid and is not an admin.
        try {
            membershipService.membershipResults(testUsername, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user matches uid and is not an admin.");
        }

        // Should not throw an exception if current user is an admin and does not match uid.
        grouperApiService.addMember(GROUPING_ADMINS, testUsername);
        try {
            membershipService.membershipResults(testUsername, ADMIN);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does not match uid.");
        } catch (UhMemberNotFoundException e) {

        }
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should not throw an exception if current user is an admin and does match uid.
        grouperApiService.addMember(GROUPING_ADMINS, testUsername);
        try {
            membershipService.membershipResults(testUsername, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does match uid.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should throw an exception if uid passed is bogus.
        try {
            membershipService.membershipResults(ADMIN, "bogus-user");
            fail("Should throw an exception if uid passed is bogus.");
        } catch (UhMemberNotFoundException e) {
            assertEquals("404 NOT_FOUND \"bogus-user\"", e.getMessage());
        }

        grouperApiService.addMember(GROUPING_BASIS, testUsername);
    }

    @Test
    public void managePersonResultsTest() {
        List<Membership> memberships;
        String testUsername = testPerson.getUsername();

        // Should not be a member.
        memberships = membershipService.managePersonResults(ADMIN, testUsername);
        assertTrue(memberships.stream()
                .noneMatch(membership -> membership.getPath().equals(GROUPING) && !membership.isInBasis()));

        // Should be a member after added.
        grouperApiService.addMember(GROUPING_OWNERS, testUsername);
        grouperApiService.addMember(GROUPING_INCLUDE, testUsername);
        grouperApiService.addMember(GROUPING_EXCLUDE, testUsername);
        grouperApiService.addMember(GROUPING_BASIS, testUsername);
        memberships = membershipService.managePersonResults(ADMIN, testUsername);
        Membership membership = memberships.stream()
                .filter(m -> m.getPath().equals(GROUPING)).findAny().orElse(null);
        assertNotNull(membership);
        assertEquals(GROUPING, membership.getPath());
        assertTrue(membership.isInExclude());
        assertTrue(membership.isInBasis());
        assertTrue(membership.isInInclude());
        assertTrue(membership.isInOwner());

        // Clean up.
        grouperApiService.removeMember(GROUPING_OWNERS, testUsername);
        grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
        grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
        grouperApiService.removeMember(GROUPING_BASIS, testUsername);

        // Should throw an exception when a non-admin user attempts to fetch memberships of another member.
        try {
            membershipService.managePersonResults(testUsername, TEST_USERNAMES.get(1));
            fail("Should throw an exception when a non-admin user attempts to fetch memberships of another member.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.managePersonResults("bogus-admin", testUsername);
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user matches uid and is not an admin.
        try {
            membershipService.managePersonResults(testUsername, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user matches uid and is not an admin.");
        }

        // Should not throw an exception if current user is an admin and does not match uid.
        grouperApiService.addMember(GROUPING_ADMINS, testUsername);
        try {
            membershipService.managePersonResults(testUsername, "bogus-user");
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does not match uid.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should not throw an exception if current user is an admin and does match uid.
        grouperApiService.addMember(GROUPING_ADMINS, testUsername);
        try {
            membershipService.managePersonResults(testUsername, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does match uid.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should return and empty list if uid passed is bogus.
        memberships = membershipService.managePersonResults(ADMIN, "bogus-user");
        assertTrue(memberships.isEmpty());
    }

    @Test
    public void updateLastModifiedTest() {
        UpdateTimestampResult updateTimestampResult = membershipService.updateLastModified(GROUPING);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_INCLUDE);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_EXCLUDE);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_BASIS);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_OWNERS);
        assertNotNull(updateTimestampResult);
    }

    @Test
    public void updateLastModifiedTimestampTest() {
        // Function updateLastModifiedTimestamp returns a complex grouper object which is wrapped into UpdateTimeResult.
        // The structure of the grouper object returned depends on three cases...
        //         i. Previous timestamp is less than new timestamp.
        //        ii. Previous timestamp is equal to new timestamp.
        //       iii. Previous timestamp is greater than new timestamp.
        // Thus, two updates are made: the epoch is first updated as the timestamp, followed by the present time.
        // This ensures case i.

        // Create a random timestamp between the epoch and now.
        LocalDateTime epoch = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT);
        LocalDateTime randomLocalDateTime = getRandomLocalDateTimeBetween(epoch, LocalDateTime.now());
        String randomDateTime = Dates.formatDate(randomLocalDateTime, "yyyyMMdd'T'HHmm");
        // Create a timestamp of present time.
        String nowDateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        // Update grouping last modified timestamp to the random timestamp.
        UpdateTimestampResult randomDateTimeResults =
                membershipService.updateLastModifiedTimestamp(randomDateTime, GROUPING_INCLUDE);
        // Update grouping last modified timestamp to present time.
        UpdateTimestampResult nowDateTimeResults =
                membershipService.updateLastModifiedTimestamp(nowDateTime, GROUPING_INCLUDE);

        // Test for case i.
        assertNotNull(randomDateTimeResults);
        assertNotNull(nowDateTimeResults);
        assertEquals(GROUPING_INCLUDE, nowDateTimeResults.getPathOfUpdate());
        assertEquals(2, nowDateTimeResults.getTimestampUpdateArray().length);
        assertEquals(nowDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[1].getWsAttributeAssignValue().getValueSystem());
        assertEquals(randomDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[0].getWsAttributeAssignValue().getValueSystem());

        // Test for case ii.
        nowDateTimeResults = membershipService.updateLastModifiedTimestamp(nowDateTime, GROUPING_INCLUDE);
        assertNotNull(nowDateTimeResults);
        assertEquals(GROUPING_INCLUDE, nowDateTimeResults.getPathOfUpdate());
        assertEquals(1, nowDateTimeResults.getTimestampUpdateArray().length);
        assertEquals(nowDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[0].getWsAttributeAssignValue().getValueSystem());
    }

    @Test
    public void getNumberOfMembershipsTest() {
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(testPerson.getUsername());
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);
        updateMemberService.removeExcludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);

        int results = membershipService.numberOfMemberships(ADMIN, testPerson.getUsername());
        updateMemberService.addIncludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);
        assertTrue(membershipService.numberOfMemberships(ADMIN, iamtst01List.get(0)) > results);
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);

        // Should have groups for user in include, basis, and not exclude only.
        iamtst01List = new ArrayList<>();
        iamtst01List.add(testPerson.getUsername());

        results = membershipService.numberOfMemberships(ADMIN, testPerson.getUsername());
        updateMemberService.addIncludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);
        updateMemberService.addExcludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);

        assertEquals(membershipService.numberOfMemberships(ADMIN, testPerson.getUsername()), results);
        updateMemberService.removeIncludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);
        updateMemberService.removeExcludeMembers(ADMIN, EMPTY_GROUPING, iamtst01List);
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
