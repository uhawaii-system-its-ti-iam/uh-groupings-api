package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingAssignmentService {

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

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    private final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    private final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    MemberAttributeService memberAttributeService;

    @Autowired
    GrouperApiService grouperApiService;

    @Autowired
    public Environment env; // Just for the settings check.

    @BeforeAll
    public void init() {
        assertTrue(memberAttributeService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUsername);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_OWNERS, testUsername);

            assertFalse(memberAttributeService.isOwner(GROUPING, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isAdmin(testUsername));
        });
    }

    @Test
    public void getGroupingTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        // Should throw and exception if current user is not an admin or and owner.
        try {
            groupingAssignmentService.getGrouping(GROUPING, iamtst01);
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        try {
            groupingAssignmentService.getGrouping(GROUPING, "bogus-user");
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupingAssignmentService.getGrouping(GROUPING, iamtst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupingAssignmentService.getGrouping(GROUPING, iamtst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }

        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.removeAdmin(ADMIN, iamtst01);
        try {
            groupingAssignmentService.getGrouping(GROUPING, iamtst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should throw and exception if a group path is passed.
        try {
            groupingAssignmentService.getGrouping(GROUPING_INCLUDE, ADMIN);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw and exception if an invalid path is passed.
        try {
            groupingAssignmentService.getGrouping("bogus-path", ADMIN);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should set all the fields of the Grouping returned.
        Grouping grouping = groupingAssignmentService.getGrouping(GROUPING, ADMIN);
        assertNotNull(grouping);
        assertNotNull(grouping.getComposite());
        assertNotNull(grouping.getPath());
        assertNotNull(grouping.getOwners());
        assertNotNull(grouping.getInclude());
        assertNotNull(grouping.getExclude());
        assertNotNull(grouping.getBasis());
        assertNotNull(grouping.getDescription());
    }

    @Test
    public void getPaginatedGroupingTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        // Should throw and exception if current user is not an admin or and owner.
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, iamtst01, null, null, null, null);
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
        // Should throw and exception if current user is not valid.
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, "bogus-user", null, null, null, null);
            fail("Should throw and exception if current user is not valid.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, iamtst01, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, iamtst01, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }

        // Should not throw an exception if current user is an owner but not an admin.
        membershipService.removeAdmin(ADMIN, iamtst01);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, iamtst01, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should throw and exception if a group path is passed.
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING_INCLUDE, ADMIN, null, null, null, null);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw and exception if an invalid path is passed.
        try {
            groupingAssignmentService.getPaginatedGrouping("bogus-path", ADMIN, null, null, null, null);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
    }

    @Test
    public void adminListsTest() {
        String iamtst01 = TEST_USERNAMES.get(0);

        // Should throw an exception if current user is not an admin.
        try {
            groupingAssignmentService.adminLists(iamtst01);
            fail("Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }

        // Should not throw an exception if current user is an admin.
        membershipService.addAdmin(ADMIN, iamtst01);
        try {
            groupingAssignmentService.adminLists(iamtst01);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }
        membershipService.removeAdmin(ADMIN, iamtst01);

        // Fields in AdminListsHolder should not be null.
        AdminListsHolder adminListsHolder = groupingAssignmentService.adminLists(ADMIN);
        assertNotNull(adminListsHolder.getAdminGroup());
        assertNotNull(adminListsHolder.getAllGroupingPaths());
    }

    @Test
    public void getMembersTest() {

        List<String> groupPaths = new ArrayList<>();
        List<String> bogusGroupPaths = new ArrayList<>();
        Collections.addAll(groupPaths, GROUPING, GROUPING_BASIS, GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
        bogusGroupPaths.add("bogus-path");

        // Should throw an exception if an invalid path is passed.
        try {
            groupingAssignmentService.getMembers(ADMIN, bogusGroupPaths);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }

        // Should throw an exception if current user is invalid.
        try {
            groupingAssignmentService.getMembers("bogus-currentUser", groupPaths);
            fail("Should throw an exception if current user is invalid.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(SUBJECT_NOT_FOUND));
        }
        Map<String, Group> groupMap = groupingAssignmentService.getMembers(ADMIN, groupPaths);
        assertNotNull(groupMap);
    }

    @Test
    public void getPaginatedMembersTest() {
        List<String> groupPaths = new ArrayList<>();
        List<String> bogusGroupPaths = new ArrayList<>();
        Collections.addAll(groupPaths, GROUPING_BASIS, GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
        bogusGroupPaths.add("bogus-path");

        // Should throw an exception if an invalid path is passed.
        try {
            groupingAssignmentService.getPaginatedMembers(ADMIN, bogusGroupPaths, null, null, null, null);
            fail("Should throw and exception if a group path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
        // Should throw an exception if current user is invalid.
        try {
            groupingAssignmentService.getPaginatedMembers("bogus-currentUser", groupPaths, null, null, null, null);
            fail("Should throw an exception if current user is invalid.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains(SUBJECT_NOT_FOUND));
        }
    }

    @Test
    public void optInOutGroupingsPathsTest() {
        // Test both getOptInGroups and getOptOutGroups()
        List<String> optInPaths = groupingAssignmentService.optInGroupingsPaths(ADMIN, TEST_USERNAMES.get(0));
        List<String> optOutPaths = groupingAssignmentService.optOutGroupingsPaths(ADMIN, TEST_USERNAMES.get(0));
        Set<String> intersection =
                optInPaths.stream().distinct().filter(optOutPaths::contains).collect(Collectors.toSet());
        // Should be no intersection between the two lists.
        assertTrue(intersection.isEmpty());
        // Should have no duplicates.
        Set<String> optInPathsMap = new HashSet<>();
        Set<String> optOutPathsMap = new HashSet<>();
        optInPaths.forEach(path -> {
            assertTrue(optInPathsMap.add(path));
            assertFalse(path.endsWith(INCLUDE));
            assertFalse(path.endsWith(EXCLUDE));
            assertFalse(path.endsWith(BASIS));
            assertFalse(path.endsWith(OWNERS));
        });
        optOutPaths.forEach(path -> {
            assertTrue(optOutPathsMap.add(path));
            assertFalse(path.endsWith(INCLUDE));
            assertFalse(path.endsWith(EXCLUDE));
            assertFalse(path.endsWith(BASIS));
            assertFalse(path.endsWith(OWNERS));
        });

    }

    @Test
    public void allGroupingsPathsTest() {
        List<String> allGroupingsPaths = groupingAssignmentService.allGroupingsPaths();
        assertNotNull(allGroupingsPaths);
    }

    @Test
    public void optableGroupingsTest() {
        List<String> optInablePaths = groupingAssignmentService.optableGroupings(OPT_IN);
        List<String> optOutablePaths = groupingAssignmentService.optableGroupings(OPT_OUT);
        assertNotNull(optInablePaths);
        assertNotNull(optOutablePaths);

        // Should not have duplicates.
        Set<String> optInpathMap = new HashSet<>();
        optInablePaths.forEach(optInablePath -> assertTrue(optInpathMap.add(optInablePath)));
        Set<String> optOutPathMap = new HashSet<>();
        optOutablePaths.forEach(optOutablePath -> assertTrue(optOutPathMap.add(optOutablePath)));

        // Should throw an exception if optIn or optOut attribute is not passed.
        try {
            groupingAssignmentService.optableGroupings("bad-attribute");
        } catch (AccessDeniedException e) {
            assertEquals(INSUFFICIENT_PRIVILEGES, e.getMessage());
        }
    }

    @Test
    public void setGroupingAttributesTest() {
        // Should set the sync destinations.
        Grouping grouping = groupingAssignmentService.setGroupingAttributes(new Grouping(GROUPING));
        assertNotNull(grouping);
        assertNotNull(grouping.getSyncDestinations());
    }

    @Test
    public void getGroupPathsTest() {
        List<String> groupPaths = groupingAssignmentService.getGroupPaths(ADMIN, ADMIN);
        assertFalse(groupPaths.isEmpty());
        // Should return an empty list if current user is not an admin and if current user is not the same as username.
        groupPaths = groupingAssignmentService.getGroupPaths(TEST_USERNAMES.get(0), TEST_USERNAMES.get(1));
        assertTrue(groupPaths.isEmpty());

        // Should return a non-empty list if current user is not an admin but is the same as username.
        groupPaths = groupingAssignmentService.getGroupPaths(TEST_USERNAMES.get(0), TEST_USERNAMES.get(0));
        assertFalse(groupPaths.isEmpty());

        // Should return a non-empty list if current user is an admin but is not the same as username.
        membershipService.addAdmin(ADMIN, TEST_USERNAMES.get(0));
        groupPaths = groupingAssignmentService.getGroupPaths(TEST_USERNAMES.get(0), TEST_USERNAMES.get(1));
        assertFalse(groupPaths.isEmpty());
        membershipService.removeAdmin(ADMIN, TEST_USERNAMES.get(0));
    }

    //ToDo add test coverage for getGroupingOwners() and isSoleOwner

}