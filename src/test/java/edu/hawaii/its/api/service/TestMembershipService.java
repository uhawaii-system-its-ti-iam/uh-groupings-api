package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMembershipService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

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

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

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
    public Environment env;

    private final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";

    @BeforeAll
    public void init() {
        assertTrue(memberAttributeService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUsername);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_OWNERS, testUsername);

            assertFalse(memberAttributeService.isMember(GROUPING_ADMINS, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_OWNERS, testUsername));
        });
    }

    @Test
    public void addAdminTest() {
        // Should throw an exception if current user is not an admin.
        String testUsername = TEST_USERNAMES.get(0);
        try {
            membershipService.addAdmin(testUsername, testUsername);
            fail(" Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not add if username is already in admins list.
        GroupingsServiceResult groupingsServiceResult;
        try {
            groupingsServiceResult = membershipService.addAdmin(ADMIN, ADMIN);
            assertNotNull(groupingsServiceResult);
            assertEquals(SUCCESS + ": " + ADMIN + " was already in" + GROUPING_ADMINS,
                    groupingsServiceResult.getResultCode());
        } catch (AccessDeniedException e) {
            fail(" Should not throw an exception if current user is an admin.");
        }

        // Should add a new admin via uh username.
        assertFalse(memberAttributeService.isAdmin(testUsername));
        groupingsServiceResult = membershipService.addAdmin(ADMIN, testUsername);
        assertTrue(memberAttributeService.isAdmin(testUsername));
        assertNotNull(groupingsServiceResult);
        //  Clean up
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should add a new admin via uh number.
        String testUhNumber = TEST_UH_NUMBERS.get(0);
        assertFalse(memberAttributeService.isAdmin(testUhNumber));
        groupingsServiceResult = membershipService.addAdmin(ADMIN, testUhNumber);
        assertTrue(memberAttributeService.isAdmin(testUhNumber));
        assertNotNull(groupingsServiceResult);
        //  Clean up
        grouperApiService.removeMember(GROUPING_ADMINS, testUhNumber);
    }

    @Test
    public void removeAdminTest() {
        // Should throw an exception if current user is not an admin.
        String testUsername = TEST_USERNAMES.get(0);
        try {
            membershipService.removeAdmin(testUsername, testUsername);
            fail(" Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should remove an admin via uh username.
        grouperApiService.addMember(GROUPING_ADMINS, testUsername);
        try {
            membershipService.removeAdmin(ADMIN, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }
        assertFalse(memberAttributeService.isAdmin(testUsername));

        // Should remove an admin via uh number.
        String testUhNumber = TEST_UH_NUMBERS.get(0);
        grouperApiService.addMember(GROUPING_ADMINS, testUhNumber);
        membershipService.removeAdmin(ADMIN, testUhNumber);
        assertFalse(memberAttributeService.isAdmin(testUhNumber));
    }

    // Todo create a ticket for rewrite.
    @Test
    public void getMembershipResultsTest() {
        List<String> iamtst01List = new ArrayList<>();
        String iamtst01 = TEST_USERNAMES.get(0);
        String iamtst02 = TEST_USERNAMES.get(1);
        iamtst01List.add(iamtst01);

        // Should have no memberships.
        List<Membership> memberships =
                getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertEquals(0, memberships.size());

        // Should have one membership after being added to include.
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);
        memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertEquals(1, memberships.size());
        Membership membership = memberships.get(0);
        // Should only be in include.
        assertTrue(membership.isInInclude());
        assertFalse(membership.isInOwner());
        assertFalse(membership.isInExclude());

        // Should have one membership after being added to exclude.
        membershipService.addExcludeMembers(ADMIN, GROUPING, iamtst01List);
        memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertEquals(1, memberships.size());
        membership = memberships.get(0);
        // Should only be in exclude.
        assertFalse(membership.isInInclude());
        assertFalse(membership.isInOwner());
        assertTrue(membership.isInExclude());

        // Should have one membership after being added to owners.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertNotNull(memberships);
        assertEquals(1, memberships.size());
        membership = memberships.get(0);
        // Should be in exclude and in owners.
        assertFalse(membership.isInInclude());
        assertTrue(membership.isInOwner());
        assertTrue(membership.isInExclude());

        // Should have one membership after being added to include.
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);
        memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertNotNull(memberships);
        assertEquals(1, memberships.size());
        membership = memberships.get(0);
        // Should be in include and in owners.
        assertTrue(membership.isInInclude());
        assertTrue(membership.isInOwner());
        assertFalse(membership.isInExclude());

        // Should have no memberships after being removed from include and owners.
        membershipService.removeIncludeMembers(ADMIN, GROUPING, iamtst01List);
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
        memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(ADMIN, iamtst01));
        assertNotNull(memberships);
        assertEquals(0, memberships.size());

        // Should throw an exception when a non-admin (username[0]) attempts to fetch the memberships
        // of another user (username[1]).
        try {
            membershipService.getMembershipResults(iamtst01, iamtst02);
            fail("Test user \"" + iamtst01 + "\" is an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should successfully fetch another user's (username[1]) memberships if username[0] is an admin.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(iamtst01, iamtst02));
            assertNotNull(memberships);
            assertTrue(memberships.isEmpty());
        } catch (AccessDeniedException e) {
            fail("Test user \"" + iamtst01 + "\" is an admin.");
        }

        //  Should throw an exception once the user is removed as admin.
        membershipService.removeAdmin(ADMIN, iamtst01);
        try {
            membershipService.getMembershipResults(iamtst01, iamtst02);
            fail("Test user \"" + iamtst01 + "\" is an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if parameters owner and uid match.
        try {
            memberships = getMembershipsForCurrentUser(membershipService.getMembershipResults(iamtst01, iamtst01));
            assertNotNull(memberships);
            assertTrue(memberships.isEmpty());
        } catch (AccessDeniedException e) {
            fail("Should pass if parameters owner and uid match");
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.getMembershipResults("bogus-admin", iamtst01);
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should return and empty list if uid passed is bogus.
        memberships = membershipService.getMembershipResults(ADMIN, "bogus-user");
        assertTrue(memberships.isEmpty());
    }

    // Todo create a ticket for rewrite.
    @Test
    public void addGroupMembersTest() {
        List<String> iamtst01List = new ArrayList<>();
        String iamtst01 = TEST_USERNAMES.get(0);
        iamtst01List.add(iamtst01);

        // Add a single user to include group, when user is not a member of include or exclude.
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
        List<AddMemberResult> addMemberResults =
                membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, iamtst01List);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getUid());
        assertEquals(memberAttributeService.getMemberAttributes(ADMIN, addMemberResult.getUid()).getUsername(),
                addMemberResult.getUid());
        assertEquals(SUCCESS, addMemberResult.getResult());
        assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        assertEquals(iamtst01List.get(0), addMemberResult.getUid());
        assertTrue(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());

        // Add a single user to include group, when the user is already a member of include.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, iamtst01List);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getUid());
        assertEquals(SUCCESS, addMemberResult.getResult());
        assertEquals(iamtst01List.get(0), addMemberResult.getUid());
        assertFalse(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());

        // Remove user for clean up.
        membershipService.removeIncludeMembers(ADMIN, GROUPING, iamtst01List);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, iamtst01List.get(0)));
        // Add a single user to exclude group, when user is not a member of include or exclude.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, iamtst01List);
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iamtst01List.get(0)));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getUid());
        assertEquals(SUCCESS, addMemberResult.getResult());
        assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
        assertEquals(iamtst01List.get(0), addMemberResult.getUid());
        assertTrue(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());

        // Add a single user to exclude group, when the user is already a member of exclude.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, iamtst01List);
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iamtst01List.get(0)));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertNotNull(addMemberResult.getUid());
        assertEquals(SUCCESS, addMemberResult.getResult());
        assertEquals(iamtst01List.get(0), addMemberResult.getUid());
        assertFalse(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());

        // Remove user for clean up.
        membershipService.removeExcludeMembers(ADMIN, GROUPING, iamtst01List);
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, iamtst01List.get(0)));

        List<String> testUsers01_03 = new ArrayList<>(TEST_USERNAMES.subList(0, 3));
        List<String> testUsers04_06 = new ArrayList<>(TEST_USERNAMES.subList(3, 6));
        // Add a list of three users to the include group.
        for (String user : testUsers01_03) {
            // Should not be in include.
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, user));
        }
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, testUsers01_03);
        for (String user : testUsers01_03) {
            // Should be in include
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, user));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers01_03.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_INCLUDE, result.getPathOfAdd());
            assertTrue(result.isUserWasAdded());
            assertFalse(result.isUserWasRemoved());
        }
        // Add a list of three users who are already in include group.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, testUsers01_03);
        for (String user : testUsers01_03) {
            // Should be in include
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, user));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers01_03.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_INCLUDE, result.getPathOfAdd());
            assertFalse(result.isUserWasAdded());
            assertFalse(result.isUserWasRemoved());
        }
        // Add a list of three users to exclude group.
        for (String user : testUsers04_06) {
            // Should not be in exclude.
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, testUsers04_06);
        for (String user : testUsers04_06) {
            // Should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers04_06.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_EXCLUDE, result.getPathOfAdd());
            assertTrue(result.isUserWasAdded());
            assertFalse(result.isUserWasRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, result.getUid()));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, result.getUid()));
        }
        // Add a list of three users who are already in exclude group.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, testUsers04_06);
        for (String user : testUsers04_06) {
            // Should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers04_06.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_EXCLUDE, result.getPathOfAdd());
            assertFalse(result.isUserWasAdded());
            assertFalse(result.isUserWasRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, result.getUid()));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, result.getUid()));
        }

        // Adding the list of test users 1-3 to exclude should remove test users 1-3 from include.
        Iterator<String> iteratorUsers01_03 = testUsers01_03.iterator();
        Iterator<String> iteratorUsers04_06 = testUsers04_06.iterator();
        while (iteratorUsers01_03.hasNext() && iteratorUsers04_06.hasNext()) {
            // Users 1 - 3 should be in include.
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iteratorUsers01_03.next()));
            // Users 4 - 6 should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iteratorUsers04_06.next()));
        }
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, testUsers01_03);
        iteratorUsers01_03 = testUsers01_03.iterator();
        iteratorUsers04_06 = testUsers04_06.iterator();
        while (iteratorUsers01_03.hasNext() && iteratorUsers04_06.hasNext()) {
            String user = iteratorUsers01_03.next();
            // Users 1 - 3 not should be in include.
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, user));
            // Users 1 - 3 should now be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
            // Users 4 - 6 should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iteratorUsers04_06.next()));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers01_03.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_EXCLUDE, result.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, result.getPathOfRemoved());
            assertTrue(result.isUserWasAdded());
            assertTrue(result.isUserWasRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, result.getUid()));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, result.getUid()));
        }

        // Adding the list of test users 4 - 6 to include should remove test users 4 - 6 from exclude.
        iteratorUsers01_03 = testUsers01_03.iterator();
        iteratorUsers04_06 = testUsers04_06.iterator();
        while (iteratorUsers01_03.hasNext() && iteratorUsers04_06.hasNext()) {
            // Users 1 - 3 should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iteratorUsers01_03.next()));
            // Users 4 - 6 should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iteratorUsers04_06.next()));
        }
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, testUsers04_06);
        iteratorUsers01_03 = testUsers01_03.iterator();
        iteratorUsers04_06 = testUsers04_06.iterator();
        while (iteratorUsers01_03.hasNext() && iteratorUsers04_06.hasNext()) {
            String user = iteratorUsers04_06.next();
            // Users 4 - 6 not should be in exclude.
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
            // Users 4 - 6 should now be in include.
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, user));
            // Users 1 - 3 should be in exclude.
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iteratorUsers01_03.next()));
        }
        assertNotNull(addMemberResults);
        assertEquals(3, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertNotNull(result.getUid());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(testUsers04_06.get(addMemberResults.indexOf(result)), result.getUid());
            assertEquals(GROUPING_INCLUDE, result.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, result.getPathOfRemoved());
            assertTrue(result.isUserWasAdded());
            assertTrue(result.isUserWasRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, result.getUid()));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, result.getUid()));
        }

        // Clean up
        membershipService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        membershipService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        for (String user : TEST_USERNAMES) {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, user));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }

        // Add a user using their uh number.
        // Fetch the admins uh number.
        List<String> adminsUhNumber = new ArrayList<>();
        membershipService.removeIncludeMembers(ADMIN, GROUPING, Arrays.asList(ADMIN));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, ADMIN));
        adminsUhNumber.add(memberAttributeService.getMemberAttributes(ADMIN, ADMIN).getUhUuid());
        assertNotNull(adminsUhNumber);
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, adminsUhNumber);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ADMIN));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        // assertNotNull(addMemberResult.getUid());
        assertEquals(SUCCESS, addMemberResult.getResult());
        assertEquals(ADMIN, addMemberResult.getUid());
        assertTrue(addMemberResult.isUserWasAdded());
        assertFalse(addMemberResult.isUserWasRemoved());

        // Clean up
        membershipService.removeIncludeMembers(ADMIN, GROUPING, adminsUhNumber);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, ADMIN));

        // Should throw an exception when a parent group path is passed.
        try {
            membershipService.addGroupMembers(ADMIN, GROUPING, TEST_USERNAMES);
            fail("Should throw an exception when a parent group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should throw an exception when an owners group path is passed.
        try {
            membershipService.addGroupMembers(ADMIN, GROUPING_OWNERS, TEST_USERNAMES);
            fail("Should throw an exception when an owners group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should throw an exception when a basis group path is passed.
        try {
            membershipService.addGroupMembers(ADMIN, GROUPING_BASIS, TEST_USERNAMES);
            fail("Should throw an exception when a basis group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should not throw an exception when a include group path is passed.
        try {
            membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        } catch (GcWebServiceError e) {
            fail("Should not throw an exception when an include group path is passed.");
        }
        // Should not throw an exception when a exclude group path is passed.
        try {
            membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, TEST_USERNAMES);
        } catch (GcWebServiceError e) {
            fail("Should not throw an exception when an exclude group path is passed.");
        }

        // Clean up
        membershipService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        for (String user : TEST_USERNAMES) {
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }

        List<String> bogusTestUsers = new ArrayList<>();
        bogusTestUsers.add("bogus-1");

        // Should not add a single bogus user to include.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, bogusTestUsers);
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertEquals(FAILURE, result.getResult());
        }

        // Should not add a single bogus user to exclude.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, bogusTestUsers);
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        for (AddMemberResult result : addMemberResults) {
            assertNotNull(result);
            assertEquals(FAILURE, result.getResult());
        }

        // Should only add one user, if a list contains one bogus and one legit user.
        bogusTestUsers.add("iamtst01"); // Add legit user to list.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, bogusTestUsers);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, bogusTestUsers.get(1)));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, bogusTestUsers.get(0)));
        assertNotNull(addMemberResults);
        assertEquals(2, addMemberResults.size());
        assertEquals(FAILURE, addMemberResults.get(0).getResult());
        assertEquals(SUCCESS, addMemberResults.get(1).getResult());

        // Clean up.
        membershipService.removeIncludeMembers(ADMIN, GROUPING, bogusTestUsers);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, bogusTestUsers.get(1)));

        // Should add when uh numbers are passed.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_UH_NUMBERS);
        TEST_UH_NUMBERS.forEach(testUhNumber -> {
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, testUhNumber));
        });
        assertNotNull(addMemberResults);
        assertEquals(TEST_UH_NUMBERS.size(), addMemberResults.size());
        addMemberResults.forEach(res -> {
            assertEquals(SUCCESS, res.getResult());
            assertTrue(TEST_UH_NUMBERS.contains(res.getUhUuid()));
        });
        // Clean up
        TEST_UH_NUMBERS.forEach(testUhNumber -> {
            grouperApiService.removeMember(GROUPING_INCLUDE, testUhNumber);
        });

    }

    @Test
    public void addIncludeMembersTest() {
        // Function addIncludeMembers() is a wrapper for addGroupMembers() that acts as a privilege guard, thus only bogus
        // usersToAdd need be passed to achieve coverage.
        List<String> bogusUsersToAdd = new ArrayList<>();
        bogusUsersToAdd.add("bogus1");
        bogusUsersToAdd.add("bogus2");

        List<AddMemberResult> addMemberResults = membershipService.addIncludeMembers(ADMIN, GROUPING, bogusUsersToAdd);
        assertNotNull(addMemberResults);
        assertEquals(2, addMemberResults.size());

        // Should not throw an exception if current user is an admin.
        try {
            membershipService.addIncludeMembers(ADMIN, GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }

        // Should not throw an exception if current user is an owner and an admin.
        List<String> adminList = new ArrayList<>();
        adminList.add(TEST_USERNAMES.get(0));
        membershipService.addOwnerships(GROUPING, ADMIN, adminList);
        assertTrue(memberAttributeService.isMember(GROUPING_OWNERS, TEST_USERNAMES.get(0)));
        try {
            membershipService.addIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner and an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, adminList);
        assertFalse(memberAttributeService.isMember(GROUPING_OWNERS, TEST_USERNAMES.get(0)));

        // Should throw an exception if currentUser is not an admin or owner.
        String iamtst01 = TEST_USERNAMES.get(0);
        try {
            membershipService.addIncludeMembers(iamtst01, GROUPING, bogusUsersToAdd);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should throw an exception if a group path is passed.
        try {
            membershipService.addIncludeMembers(ADMIN, GROUPING_INCLUDE, bogusUsersToAdd);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw an exception if an invalid path is passed.
        try {
            membershipService.addIncludeMembers(ADMIN, "bad-path", bogusUsersToAdd);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should not throw an exception if current user is not an admin but is an owner of the group.
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        membershipService.removeAdmin(ADMIN, iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        assertFalse(memberAttributeService.isAdmin(iamtst01));
        assertTrue(memberAttributeService.isOwner(GROUPING, iamtst01));
        try {
            membershipService.addIncludeMembers(iamtst01, GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is not an admin but is an owner of the group.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01));
    }

    @Test
    public void addExcludeMembersTest() {
        // Function addExcludeMembers() is a wrapper for addGroupMembers() that acts as a privilege guard, thus only bogus
        // usersToAdd need be passed to achieve coverage.
        List<String> bogusUsersToAdd = new ArrayList<>();
        bogusUsersToAdd.add("bogus1");
        bogusUsersToAdd.add("bogus2");

        List<AddMemberResult> addMemberResults = membershipService.addExcludeMembers(ADMIN, GROUPING, bogusUsersToAdd);
        assertNotNull(addMemberResults);
        assertEquals(2, addMemberResults.size());

        // Should not throw an exception if current user is an admin.
        try {
            membershipService.addExcludeMembers(ADMIN, GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }

        // Should not throw an exception if current user is an owner and an admin.
        List<String> adminList = new ArrayList<>();
        adminList.add(TEST_USERNAMES.get(0));
        membershipService.addOwnerships(GROUPING, ADMIN, adminList);
        assertTrue(memberAttributeService.isMember(GROUPING_OWNERS, TEST_USERNAMES.get(0)));
        try {
            membershipService.addExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner and an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, adminList);
        assertFalse(memberAttributeService.isMember(GROUPING_OWNERS, TEST_USERNAMES.get(0)));

        // Should throw an exception if currentUser is not an admin or owner.
        String iamtst01 = TEST_USERNAMES.get(0);
        try {
            membershipService.addExcludeMembers(iamtst01, GROUPING, bogusUsersToAdd);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should throw an exception if a group path is passed.
        try {
            membershipService.addExcludeMembers(ADMIN, GROUPING_INCLUDE, bogusUsersToAdd);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw an exception if an invalid path is passed.
        try {
            membershipService.addExcludeMembers(ADMIN, "bad-path", bogusUsersToAdd);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
        // Should not throw an exception if current user is not an admin but is an owner of the group.
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        membershipService.removeAdmin(ADMIN, iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        assertFalse(memberAttributeService.isAdmin(iamtst01));
        assertTrue(memberAttributeService.isOwner(GROUPING, iamtst01));
        try {
            membershipService.addExcludeMembers(iamtst01, GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is not an admin but is an owner of the group.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01));

    }

    @Test
    public void removeGroupMembersTest() {
        List<RemoveMemberResult> removeMemberResults;
        // Should remove users by passing uh usernames.
        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.addMember(GROUPING_INCLUDE, testUsername);
        });
        removeMemberResults = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        TEST_USERNAMES.forEach(testUserName -> {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUserName));
        });
        assertNotNull(removeMemberResults);
        assertEquals(TEST_USERNAMES.size(), removeMemberResults.size());
        removeMemberResults.forEach(removeMemberResult -> {
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
            assertTrue(TEST_USERNAMES.contains(removeMemberResult.getUid()));
        });

        // Should remove users by passing uh numbers.
        TEST_UH_NUMBERS.forEach(uhNumber -> {
            grouperApiService.addMember(GROUPING_INCLUDE, uhNumber);
        });
        removeMemberResults = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_UH_NUMBERS);
        TEST_UH_NUMBERS.forEach(uhNumber -> {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, uhNumber));
        });
        assertNotNull(removeMemberResults);
        assertEquals(TEST_UH_NUMBERS.size(), removeMemberResults.size());
        removeMemberResults.forEach(removeMemberResult -> {
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
            assertTrue(TEST_UH_NUMBERS.contains(removeMemberResult.getUhUuid()));
        });

        // Should throw an exception when a parent group path is passed.
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING, TEST_USERNAMES);
            fail("Should throw an exception when a parent group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should throw an exception when an owners group path is passed.
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING_OWNERS, TEST_USERNAMES);
            fail("Should throw an exception when an owners group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should throw an exception when a basis group path is passed.
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING_OWNERS, TEST_USERNAMES);
            fail("Should throw an exception when a basis group path is passed.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }
        // Should not throw an exception when a include group path is passed.
        membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        } catch (GcWebServiceError e) {
            fail("Should not throw an exception when an include group path is passed.");
        }
        // Should not throw an exception when a exclude group path is passed.
        membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, TEST_USERNAMES);
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING_EXCLUDE, TEST_USERNAMES);
        } catch (GcWebServiceError e) {
            fail("Should not throw an exception when an exclude group path is passed.");
        }
        // Clean up
        membershipService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        for (String user : TEST_USERNAMES) {
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, user));
        }
    }

    @Test
    public void removeIncludeMembersTest() {
        // Function removeIncludeMembers() is a wrapper for removeGroupMembers() that acts as a privilege guard, thus only bogus
        // usersToAdd need be passed to achieve coverage.
        List<String> bogusUsersToRemove = new ArrayList<>();
        String iamtst01 = TEST_USERNAMES.get(0);
        bogusUsersToRemove.add("bogus1");
        bogusUsersToRemove.add("bogus2");

        // Should not throw an exception if current user is an admin.
        try {
            membershipService.removeIncludeMembers(ADMIN, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }

        // Should not throw an exception if current user is an owner and an admin.
        List<String> adminList = new ArrayList<>();
        adminList.add(iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, adminList);
        try {
            membershipService.removeIncludeMembers(iamtst01, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner and an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, adminList);

        // Should throw an exception if currentUser is not an admin or owner.
        try {
            membershipService.removeIncludeMembers(iamtst01, GROUPING, null);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should throw an exception if a group path is passed.
        try {
            membershipService.removeIncludeMembers(ADMIN, GROUPING_INCLUDE, null);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw an exception if an invalid path is passed.
        try {
            membershipService.removeIncludeMembers(iamtst01, "bad-path", null);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
        // Should not throw an exception if current user is not an admin but is an owner of the group.
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        membershipService.removeAdmin(ADMIN, iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            membershipService.removeIncludeMembers(iamtst01, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is not an admin but is an owner of the group.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
    }

    @Test
    public void removeExcludeMembersTest() {
        // Function removeExcludeMembers() is a wrapper for removeGroupMembers() that acts as a privilege guard, thus only bogus
        // usersToAdd need be passed to achieve coverage.
        List<String> bogusUsersToRemove = new ArrayList<>();
        String iamtst01 = TEST_USERNAMES.get(0);
        bogusUsersToRemove.add("bogus1");
        bogusUsersToRemove.add("bogus2");

        // Should not throw an exception if current user is an admin.
        try {
            membershipService.removeExcludeMembers(ADMIN, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }

        // Should not throw an exception if current user is an owner and an admin.
        List<String> adminList = new ArrayList<>();
        adminList.add(iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, adminList);
        try {
            membershipService.removeExcludeMembers(iamtst01, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner and an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, adminList);

        // Should throw an exception if currentUser is not an admin or owner.
        try {
            membershipService.removeExcludeMembers(iamtst01, GROUPING, null);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should throw an exception if a group path is passed.
        try {
            membershipService.removeExcludeMembers(ADMIN, GROUPING_INCLUDE, null);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw an exception if an invalid path is passed.
        try {
            membershipService.removeExcludeMembers(iamtst01, "bad-path", null);
            fail("Should throw an exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
        // Should not throw an exception if current user is not an admin but is an owner of the group.
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        membershipService.removeAdmin(ADMIN, iamtst01);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            membershipService.removeExcludeMembers(iamtst01, GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is not an admin but is an owner of the group.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

    }

    @Test
    public void removeOwnershipsTest() {
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));

        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        // Should remove a single owner.
        try {
            removeMemberResults = membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception when current user is admin.");
        }
        assertNotNull(removeMemberResults);
        assertEquals(1, removeMemberResults.size());
        RemoveMemberResult removeMemberResult = removeMemberResults.get(0);
        assertNotNull(removeMemberResult);
        assertEquals(iamtst01List.get(0), removeMemberResult.getUid());
        assertEquals(GROUPING_OWNERS, removeMemberResult.getPathOfRemoved());
        assertTrue(removeMemberResult.isUserWasRemoved());
        assertFalse(memberAttributeService.isMember(GROUPING_OWNERS, removeMemberResult.getUid()));

        // Should remove multiple owners.
        membershipService.addOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        try {
            removeMemberResults = membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception when current user is admin.");
        }
        assertNotNull(removeMemberResults);
        assertEquals(TEST_USERNAMES.size(), removeMemberResults.size());
        for (RemoveMemberResult removeResult : removeMemberResults) {
            assertNotNull(removeResult);
            assertEquals(TEST_USERNAMES.get(removeMemberResults.indexOf(removeResult)), removeResult.getUid());
            assertEquals(GROUPING_OWNERS, removeResult.getPathOfRemoved());
            assertTrue(removeResult.isUserWasRemoved());
            assertFalse(memberAttributeService.isOwner(GROUPING, removeMemberResult.getUid()));
        }

        // Should throw an exception if current user is not an admin or owner.
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isAdmin(iamtst01List.get(0)));
        try {
            membershipService.removeOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
            fail("Should throw an exception if current user is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        assertTrue(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isAdmin(iamtst01List.get(0)));
        // Should not throw an exception if the current user is not an admin but is an owner.
        try {
            membershipService.removeOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
        } catch (AccessDeniedException e) {
            fail(" Should not throw an exception if the current user is not an admin but is an owner.");
        }
    }

    @Test
    public void addOwnershipsTest() {
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));

        // Should add a single owner.
        try {
            addMemberResults = membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception when current user is admin.");
        }
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertEquals(iamtst01List.get(0), addMemberResult.getUid());
        assertEquals(GROUPING_OWNERS, addMemberResult.getPathOfAdd());
        assertTrue(addMemberResult.isUserWasAdded());
        assertTrue(memberAttributeService.isMember(GROUPING_OWNERS, addMemberResult.getUid()));
        // Clean up.
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should add multiple owners.
        try {
            addMemberResults = membershipService.addOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception when current user is admin.");
        }
        assertNotNull(addMemberResults);
        assertEquals(TEST_USERNAMES.size(), addMemberResults.size());
        for (AddMemberResult addResult : addMemberResults) {
            assertNotNull(addResult);
            assertEquals(TEST_USERNAMES.get(addMemberResults.indexOf(addResult)), addResult.getUid());
            assertEquals(GROUPING_OWNERS, addResult.getPathOfAdd());
            assertTrue(addResult.isUserWasAdded());
            assertTrue(memberAttributeService.isOwner(GROUPING, addResult.getUid()));
        }
        // Clean up.
        membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);

        // Should throw an exception if current user is not an admin or owner.
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isAdmin(iamtst01List.get(0)));
        try {
            membershipService.addOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
            fail("Should throw an exception if current user is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        // Should not throw an exception if the current user is not an admin but is an owner.
        assertTrue(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isAdmin(iamtst01List.get(0)));
        try {
            membershipService.addOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
        } catch (AccessDeniedException e) {
            fail(" Should not throw an exception if the current user is not an admin but is an owner.");
        }
        // Clean up.
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
    }

    @Test
    public void optInTest() {
        String bogusUser = "bogus-user";
        String iamstst01 = TEST_USERNAMES.get(0);
        List<String> iamstst01List = new ArrayList<>();
        iamstst01List.add(iamstst01);
        // Should throw an exception if current user is not and admin or if the uid opting is not equal to current user.
        try {
            membershipService.optIn(iamstst01, GROUPING, bogusUser);
            fail("Should throw an exception if current user is not and admin or if the uid opting is not equal to current user.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if the uid opting is not equal to current user, but current user is and admin.
        try {
            membershipService.optIn(ADMIN, GROUPING, bogusUser);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the uid opting is not equal to current user, but current user is an admin.");
        }
        // Should not throw an exception if the current user is not an admin, but current user does equal uid.
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        try {
            addMemberResults = membershipService.optIn(iamstst01, GROUPING, iamstst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the current user is not an admin, but current user does equal uid.");
        }
        // User iamtst01 should be in the include group.
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iamstst01));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertTrue(addMemberResult.isUserWasAdded());
        assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        assertFalse(addMemberResult.isUserWasRemoved());
        assertEquals(iamstst01, addMemberResult.getUid());

        // Should remove user from exclude then add it to include.
        membershipService.addExcludeMembers(ADMIN, GROUPING, iamstst01List);
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iamstst01));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamstst01));
        assertNotNull(membershipService.optIn(ADMIN, GROUPING, iamstst01));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, iamstst01));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iamstst01));

        membershipService.removeIncludeMembers(ADMIN, GROUPING, iamstst01List);
    }

    @Test
    public void optOutTest() {
        String bogusUser = "bogus-user";
        String iamstst01 = TEST_USERNAMES.get(0);
        List<String> iamstst01List = new ArrayList<>();
        iamstst01List.add(iamstst01);

        // Should throw an exception if current user is not and admin or if the uid opting is not equal to current user.
        try {
            membershipService.optOut(iamstst01, GROUPING, bogusUser);
            fail("Should throw an exception if current user is not and admin or if the uid opting is not equal to current user.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should not throw an exception if the uid opting is not equal to current user, but current user is and admin.
        try {
            membershipService.optOut(ADMIN, GROUPING, bogusUser);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the uid opting is not equal to current user, but current user is an admin.");
        }
        // Should not throw an exception if the current user is not an admin, but current user does equal uid.
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        try {

            addMemberResults = membershipService.optOut(iamstst01, GROUPING, iamstst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the current user is not an admin, but current user does equal uid.");
        }
        // User iamtst01 should be in the exclude group.
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iamstst01));
        assertNotNull(addMemberResults);
        assertEquals(1, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertNotNull(addMemberResult);
        assertTrue(addMemberResult.isUserWasAdded());
        assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
        assertFalse(addMemberResult.isUserWasRemoved());
        assertEquals(iamstst01, addMemberResult.getUid());

        // Should remove user from include and add it to include.
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamstst01List);
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, iamstst01));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, iamstst01));
        assertNotNull(membershipService.optOut(ADMIN, GROUPING, iamstst01));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, iamstst01));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamstst01));

        membershipService.removeExcludeMembers(ADMIN, GROUPING, iamstst01List);

    }

    @Test
    public void removeFromGroupsTest() {
        // Should throw an exception if current user is not an admin.
        try {
            membershipService.removeFromGroups(TEST_USERNAMES.get(0), null, null);
            fail("Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should remove userToRemove from the groups listed in groupPaths.
        List<String> iamtst01List = new ArrayList<>();
        List<String> pathList = new ArrayList<>();
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));
        pathList.add(GROUPING_OWNERS);
        pathList.add(GROUPING_INCLUDE);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);
        try {
            removeMemberResults = membershipService.removeFromGroups(ADMIN, iamtst01List.get(0), pathList);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception is current user is an admin.");
        }
        assertNotNull(removeMemberResults);
        assertEquals(2, removeMemberResults.size());
        RemoveMemberResult removeMemberResult = removeMemberResults.get(0);
        assertNotNull(removeMemberResult);
        assertEquals(GROUPING_OWNERS, removeMemberResult.getPathOfRemoved());
        removeMemberResult = removeMemberResults.get(1);
        assertNotNull(removeMemberResult);
        assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
    }

    @Test
    public void removeIncludeExcludeMembersTest() {
        List<String> uhNumbersInclude = TEST_USERNAMES.subList(0, 3);
        List<String> uhNumbersExclude = TEST_USERNAMES.subList(3, 6);
        assertNotNull(membershipService.addIncludeMembers(ADMIN, GROUPING, uhNumbersInclude));
        assertNotNull(membershipService.addExcludeMembers(ADMIN, GROUPING, uhNumbersExclude));

        // Should remove all users passed as uhNumbersInclude and uhNumbersExclude.
        List<RemoveMemberResult> removeMemberResults =
                membershipService.resetGroup(ADMIN, GROUPING, uhNumbersInclude, uhNumbersExclude);
        assertEquals(TEST_USERNAMES, removeMemberResults
                .stream().map(RemoveMemberResult::getUid).collect(Collectors.toList()));
        assertTrue(removeMemberResults.subList(0, 3).stream()
                .allMatch(removeMemberResult -> removeMemberResult.getPathOfRemoved().equals(GROUPING_INCLUDE)));
        assertTrue(removeMemberResults.subList(3, 6).stream()
                .allMatch(removeMemberResult -> removeMemberResult.getPathOfRemoved().equals(GROUPING_EXCLUDE)));
        uhNumbersInclude.forEach(uhNumber ->
                assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, uhNumber)));
        uhNumbersExclude.forEach(uhNumber ->
                assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, uhNumber)));

        // Should throw an exception if current user is not an admin or an owner.
        try {
            membershipService.resetGroup(TEST_USERNAMES.get(0), GROUPING, null, null);
            fail("Should throw an exception if current user is not an admin or an owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        uhNumbersInclude = new ArrayList<>();
        uhNumbersExclude = new ArrayList<>();
        // Should not throw an exception if current user is not an admin but is an owner.
        List<String> iamtst01List = TEST_USERNAMES.subList(0, 1);
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        assertTrue(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        try {
            assertTrue(membershipService.resetGroup(iamtst01List.get(0), GROUPING, uhNumbersInclude, uhNumbersExclude)
                    .isEmpty()); // Results should be empty.
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is not an admin but is an owner.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01List.get(0));
        assertTrue(memberAttributeService.isAdmin(iamtst01List.get(0)));
        try {
            assertTrue(membershipService.resetGroup(iamtst01List.get(0), GROUPING, uhNumbersInclude, uhNumbersExclude)
                    .isEmpty());
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01List.get(0));

        // Should not throw and exception if user is an admin and an owner.
        try {
            assertTrue(membershipService.resetGroup(ADMIN, GROUPING, uhNumbersInclude, uhNumbersExclude).isEmpty());
        } catch (AccessDeniedException e) {
            fail(" Should not throw and exception if user is an admin and an owner.");
        }
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
        iamtst01List.add(TEST_USERNAMES.get(0));
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);
        assertTrue(membershipService.getNumberOfMemberships(ADMIN, iamtst01List.get(0)) > 0);
        membershipService.removeIncludeMembers(ADMIN, GROUPING, iamtst01List);
    }

    /**
     * Create a sublist which contains all memberships whose path contains the currentUsers uh username (ADMIN) and
     * who is not in basis. This is a helper method for getMembershipResultsTest().
     */
    private List<Membership> getMembershipsForCurrentUser(List<Membership> memberships) {
        return memberships
                .stream().filter(membership -> membership.getPath().contains(GROUPING))
                .collect(Collectors.toList())
                .stream().filter(membership -> !membership.isInBasis()).collect(Collectors.toList());
    }

    /**
     * Get a random LocalDateTime between start and end. This is a helper method for updateLastModifiedTimestampTest().
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
     * Get a random number between start and end. This is a helper method for getRandomLocalDateTimeBetween().
     */
    private static int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
}
