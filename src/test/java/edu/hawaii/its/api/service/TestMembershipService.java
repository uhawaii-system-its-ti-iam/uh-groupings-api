package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.util.JsonUtil;

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
        });
        TEST_UH_NUMBERS.forEach(testNumber -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testNumber);
            grouperApiService.removeMember(GROUPING_INCLUDE, testNumber);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testNumber);
            grouperApiService.removeMember(GROUPING_OWNERS, testNumber);
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
        AddMemberResult addMemberResult;
        try {
            addMemberResult = membershipService.addAdmin(ADMIN, ADMIN);
            assertNotNull(addMemberResult);
            assertEquals(FAILURE, addMemberResult.getResult());
        } catch (AccessDeniedException e) {
            fail(" Should not throw an exception if current user is an admin.");
        }

        // Should add a new admin via uh username.
        assertFalse(memberAttributeService.isAdmin(testUsername));
        addMemberResult = membershipService.addAdmin(ADMIN, testUsername);
        assertTrue(memberAttributeService.isAdmin(testUsername));
        assertNotNull(addMemberResult);
        assertEquals(SUCCESS, addMemberResult.getResult());
        //  Clean up
        grouperApiService.removeMember(GROUPING_ADMINS, testUsername);

        // Should add a new admin via uh number.
        String testUhNumber = TEST_UH_NUMBERS.get(0);
        assertFalse(memberAttributeService.isAdmin(testUhNumber));
        addMemberResult = membershipService.addAdmin(ADMIN, testUhNumber);
        assertTrue(memberAttributeService.isAdmin(testUhNumber));
        assertNotNull(addMemberResult);
        assertEquals(SUCCESS, addMemberResult.getResult());
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

    @Test
    public void membershipResultsTest() {
        List<Membership> memberships;

        // Should not be a member.
        memberships = membershipService.membershipResults(ADMIN, TEST_USERNAMES.get(0));
        assertTrue(memberships.stream()
                .noneMatch(membership -> membership.getPath().equals(GROUPING) && !membership.isInBasis()));

        // Should be a member after added.
        grouperApiService.addMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.addMember(GROUPING_EXCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.addMember(GROUPING_BASIS, TEST_USERNAMES.get(0));
        memberships = membershipService.membershipResults(ADMIN, TEST_USERNAMES.get(0));
        Membership membership = memberships.stream()
                .filter(m -> m.getPath().equals(GROUPING)).findAny().orElse(null);
        assertNotNull(membership);
        assertEquals(GROUPING, membership.getPath());
        assertTrue(membership.isInExclude());
        assertTrue(membership.isInBasis());
        assertTrue(membership.isInInclude());
        assertTrue(membership.isInOwner());
        // Clean up.
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.removeMember(GROUPING_EXCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.removeMember(GROUPING_BASIS, TEST_USERNAMES.get(0));

        // Should throw an exception when a non-admin user attempts to fetch memberships of another member.
        try {
            membershipService.membershipResults(TEST_USERNAMES.get(0), TEST_USERNAMES.get(1));
            fail("Should throw an exception when a non-admin user attempts to fetch memberships of another member.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should throw an exception if bogus-admin is passed as owner.
        try {
            membershipService.membershipResults("bogus-admin", TEST_USERNAMES.get(0));
            fail("Should throw exception if bogus-admin is passed as owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if current user matches uid and is not an admin.
        try {
            membershipService.membershipResults(TEST_USERNAMES.get(0), TEST_USERNAMES.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user matches uid and is not an admin.");
        }

        // Should not throw an exception if current user is an admin and does not match uid.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.membershipResults(TEST_USERNAMES.get(0), "bogus-user");
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does not match uid.");
        }

        // Should not throw an exception if current user is an admin and does match uid.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.membershipResults(TEST_USERNAMES.get(0), TEST_USERNAMES.get(0));
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and does match uid.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));

        // Should return and empty list if uid passed is bogus.
        memberships = membershipService.membershipResults(ADMIN, "bogus-user");
        assertTrue(memberships.isEmpty());
    }

    @Test
    public void addGroupMembersTest() {
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

        // Should add users from a list of uh usernames.
        List<AddMemberResult> addMemberResults =
                membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        assertEquals(TEST_USERNAMES.size(), addMemberResults.size());
        assertTrue(addMemberResults.stream().map(AddMemberResult::getUid).collect(Collectors.toList())
                .containsAll(TEST_USERNAMES));
        addMemberResults.forEach(addMemberResult -> {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertTrue(addMemberResult.isUserWasAdded());
            assertFalse(addMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        });
        TEST_USERNAMES.forEach(testUsername ->
                assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername)));

        // Should add users from a list of uh numbers.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_UH_NUMBERS);
        assertEquals(TEST_UH_NUMBERS.size(), addMemberResults.size());
        assertTrue(addMemberResults.stream().map(AddMemberResult::getUhUuid).collect(Collectors.toList())
                .containsAll(TEST_UH_NUMBERS));
        addMemberResults.forEach(addMemberResult -> {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertTrue(addMemberResult.isUserWasAdded());
            assertFalse(addMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
        });
        TEST_UH_NUMBERS.forEach(testNumber ->
                assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, testNumber)));
        // Clean up.
        TEST_UH_NUMBERS.forEach(testNumber ->
                grouperApiService.removeMember(GROUPING_INCLUDE, testNumber));

        // Should not add users that are already in the list.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        assertEquals(TEST_USERNAMES.size(), addMemberResults.size());
        addMemberResults.forEach(addMemberResult ->
                assertFalse(addMemberResult.isUserWasAdded()));

        // Should move users from include to exclude if adding to exclude and user already exists in include.
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_EXCLUDE, TEST_USERNAMES);
        addMemberResults.forEach(addMemberResult -> {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertTrue(addMemberResult.isUserWasAdded());
            assertTrue(addMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfRemoved());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
        });
        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
        });
        // Clean up.
        TEST_USERNAMES.forEach(testUsername -> grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername));

        // Should return a failed AddMemberResult if user to add is invalid.
        List<String> bogusUserToAdd = new ArrayList<>();
        bogusUserToAdd.add("bogus-user");
        addMemberResults = membershipService.addGroupMembers(ADMIN, GROUPING_INCLUDE, bogusUserToAdd);
        assertTrue(addMemberResults.stream().allMatch(addMemberResult -> addMemberResult.getResult().equals(FAILURE)));
        JsonUtil.printJson(addMemberResults);

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

        // Should not throw an exception if current user is an owner but not an admin.
        grouperApiService.addMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        try {
            membershipService.addIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.addIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

        // Should not throw an exception if current user is admin but not an owner.
        try {
            membershipService.addIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is admin but not an owner.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));

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

        // Should not throw an exception if current user is an owner but not an admin.
        grouperApiService.addMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        try {
            membershipService.addExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.addExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

        // Should not throw an exception if current user is admin but not an owner.
        try {
            membershipService.addExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToAdd);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is admin but not an owner.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));

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
        TEST_USERNAMES.forEach(testUsername -> grouperApiService.addMember(GROUPING_INCLUDE, testUsername));
        removeMemberResults = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        TEST_USERNAMES.forEach(
                testUserName -> assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUserName)));
        assertNotNull(removeMemberResults);
        assertEquals(TEST_USERNAMES.size(), removeMemberResults.size());
        removeMemberResults.forEach(removeMemberResult -> {
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
            assertTrue(TEST_USERNAMES.contains(removeMemberResult.getUid()));
        });

        // Should remove users by passing uh numbers.
        TEST_UH_NUMBERS.forEach(uhNumber -> grouperApiService.addMember(GROUPING_INCLUDE, uhNumber));
        removeMemberResults = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_UH_NUMBERS);
        TEST_UH_NUMBERS.forEach(uhNumber -> assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, uhNumber)));
        assertNotNull(removeMemberResults);
        assertEquals(TEST_UH_NUMBERS.size(), removeMemberResults.size());
        removeMemberResults.forEach(removeMemberResult -> {
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
            assertTrue(TEST_UH_NUMBERS.contains(removeMemberResult.getUhUuid()));
        });

        // Should return a failed RemoveMemberResult if user to remove is invalid.
        List<String> bogusUserToRemove = new ArrayList<>();
        bogusUserToRemove.add("bogus-user");
        removeMemberResults = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, bogusUserToRemove);
        assertTrue(removeMemberResults.stream()
                .allMatch(removeMemberResult -> removeMemberResult.getResult().equals(FAILURE)));

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
        // Should not throw an exception when an include group path is passed.
        TEST_USERNAMES.forEach(testUsername -> grouperApiService.addMember(GROUPING_INCLUDE, testUsername));
        try {
            membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, TEST_USERNAMES);
        } catch (GcWebServiceError e) {
            fail("Should not throw an exception when an include group path is passed.");
        }
        // Should not throw an exception when an exclude group path is passed.
        TEST_USERNAMES.forEach(testUsername -> grouperApiService.addMember(GROUPING_EXCLUDE, testUsername));
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

        // Should not throw an exception if current user is an owner but not an admin.
        grouperApiService.addMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        try {
            membershipService.removeIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.removeIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

        // Should not throw an exception if current user is admin but not an owner.
        try {
            membershipService.removeIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is admin but not an owner.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));

        // Should throw an exception if currentUser is not an admin or owner.
        try {
            membershipService.removeIncludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

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

        // Should not throw an exception if current user is an owner but not an admin.
        grouperApiService.addMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));
        try {
            membershipService.removeExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        grouperApiService.addMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));
        try {
            membershipService.removeExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

        // Should not throw an exception if current user is admin but not an owner.
        try {
            membershipService.removeExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is admin but not an owner.");
        }
        grouperApiService.removeMember(GROUPING_ADMINS, TEST_USERNAMES.get(0));

        // Should throw an exception if currentUser is not an admin or owner.
        try {
            membershipService.removeExcludeMembers(TEST_USERNAMES.get(0), GROUPING, bogusUsersToRemove);
            fail("Should throw an exception if currentUser is not an admin or owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        grouperApiService.removeMember(GROUPING_OWNERS, TEST_USERNAMES.get(0));

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
            membershipService.addOwnerships(GROUPING, iamtst01List.get(0), null);
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

        // Should not throw an exception if the current user is an admin an and an owner.
        grouperApiService.addMember(GROUPING_ADMINS, iamtst01List.get(0));
        try {
            membershipService.addOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the current user is an admin an and an owner.");
        }

        // Should not throw an exception if the current user is an admin an and not an owner.
        grouperApiService.removeMember(GROUPING_OWNERS, iamtst01List.get(0));
        try {
            membershipService.addOwnerships(GROUPING, iamtst01List.get(0), iamtst01List);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if the current user is an admin an and not an owner.");
        }
        // Clean up.
        grouperApiService.removeMember(GROUPING_ADMINS, iamtst01List.get(0));
        grouperApiService.removeMember(GROUPING_OWNERS, iamtst01List.get(0));
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
        // Should throw an exception if groupPaths contains paths that are not include, exclude, or owners.
        List<String> badPaths = new ArrayList<>();
        badPaths.add(GROUPING_BASIS);
        badPaths.add(GROUPING);
        try {
            membershipService.removeFromGroups(ADMIN, TEST_USERNAMES.get(0), badPaths);
            fail("Should throw an exception if groupPaths contains paths that are not include, exclude, or owners.");
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject());
        }

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
                .stream().filter(membership -> membership.getPath().equals(GROUPING))
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
