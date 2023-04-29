package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingsGroupMembers;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

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

    @Autowired private UpdateMemberService updateMemberService;

    @Autowired private MemberService memberService;

    @Autowired
    public Environment env; // Just for the settings check.

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private Person testPerson;

    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUsername);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUsername);
            grouperApiService.removeMember(GROUPING_OWNERS, testUsername);

            assertFalse(memberService.isOwner(GROUPING, testUsername));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberService.isAdmin(testUsername));
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
    public void getGroupingTest() {
        String testUsername = testPerson.getUsername();
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(testUsername);
        updateMemberService.removeAdmin(ADMIN, testUsername);
        // Should throw and exception if current user is not an admin or and owner.
        try {
            groupingAssignmentService.getGrouping(GROUPING, testUsername);
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }
        try {
            groupingAssignmentService.getGrouping(GROUPING, "bogus-user");
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUsername);
        try {
            groupingAssignmentService.getGrouping(GROUPING, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        updateMemberService.addOwnerships(ADMIN, GROUPING, iamtst01List);
        try {
            groupingAssignmentService.getGrouping(GROUPING, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }

        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.removeAdmin(ADMIN, testUsername);
        try {
            groupingAssignmentService.getGrouping(GROUPING, testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, iamtst01List);

        // Should throw and exception if a group path is passed.
        assertThrows(NullPointerException.class, () -> groupingAssignmentService.getGrouping(GROUPING_INCLUDE, ADMIN));

        // Should throw and exception if an invalid path is passed.
        assertThrows(NullPointerException.class, () -> groupingAssignmentService.getGrouping("bogus-path", ADMIN));

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
        String testUsername = testPerson.getUsername();
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(testUsername);
        // Should throw and exception if current user is not an admin or and owner.
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, testUsername, null, null, null, null);
            fail("Should throw and exception if current user is not an admin or and owner.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }
        // Should throw and exception if current user is not valid.
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, "bogus-user", null, null, null, null);
            fail("Should throw and exception if current user is not valid.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUsername);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, testUsername, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin but not an owner.");
        }

        // Should not throw an exception if current user is an admin and an owner.
        updateMemberService.addOwnerships(ADMIN, GROUPING, iamtst01List);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, testUsername, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin and an owner.");
        }

        // Should not throw an exception if current user is an owner but not an admin.
        updateMemberService.removeAdmin(ADMIN, testUsername);
        try {
            groupingAssignmentService.getPaginatedGrouping(GROUPING, testUsername, null, null, null, null);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an owner but not an admin.");
        }
        updateMemberService.removeOwnerships(ADMIN, GROUPING, iamtst01List);
        // Should throw and exception if an invalid path is passed.
        assertThrows(NullPointerException.class,
                () -> groupingAssignmentService.getPaginatedGrouping("bogus-path", ADMIN, null, null, null, null));
    }

    @Test
    public void adminListsTest() {
        String testUsername = testPerson.getUsername();

        // Should throw an exception if current user is not an admin.
        try {
            groupingAssignmentService.adminLists(testUsername);
            fail("Should throw an exception if current user is not an admin.");
        } catch (AccessDeniedException e) {
            assertEquals("Insufficient Privileges", e.getMessage());
        }

        // Should not throw an exception if current user is an admin.
        updateMemberService.addAdmin(ADMIN, testUsername);
        try {
            groupingAssignmentService.adminLists(testUsername);
        } catch (AccessDeniedException e) {
            fail("Should not throw an exception if current user is an admin.");
        }
        updateMemberService.removeAdmin(ADMIN, testUsername);

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
        String testUsername = testPerson.getUsername();
        // Test both getOptInGroups and getOptOutGroups()
        List<GroupingPath> optInGroupingsPaths =
                groupingAssignmentService.optInGroupingPaths(ADMIN, testUsername);
        List<String> optInPaths = optInGroupingsPaths.stream().map(GroupingPath::getPath).collect(Collectors.toList());
        List<String> optOutPaths = groupingAssignmentService.optOutGroupingsPaths(ADMIN, testUsername);
        Set<String> intersection =
                optInPaths.stream().distinct().filter(optOutPaths::contains).collect(Collectors.toSet());
        // Should be no intersection between the two lists.
        assertTrue(intersection.isEmpty());
        // Should have no duplicates.
        Set<String> optInPathsMap = new HashSet<>();
        Set<String> optOutPathsMap = new HashSet<>();
        optInPaths.forEach(path -> {
            assertTrue(optInPathsMap.add(path));
            assertFalse(path.endsWith(GroupType.INCLUDE.value()));
            assertFalse(path.endsWith(GroupType.EXCLUDE.value()));
            assertFalse(path.endsWith(GroupType.BASIS.value()));
            assertFalse(path.endsWith(GroupType.OWNERS.value()));
            assertTrue(groupAttributeService.isGroupAttribute(path, OptType.IN.value()));
        });
        optOutPaths.forEach(path -> {
            assertTrue(optOutPathsMap.add(path));
            assertFalse(path.endsWith(GroupType.INCLUDE.value()));
            assertFalse(path.endsWith(GroupType.EXCLUDE.value()));
            assertFalse(path.endsWith(GroupType.BASIS.value()));
            assertFalse(path.endsWith(GroupType.OWNERS.value()));
            assertTrue(groupAttributeService.isGroupAttribute(path, OptType.OUT.value()));
        });

    }

    @Test
    public void setGroupingAttributesTest() {
        // Should set the sync destinations.
        Grouping grouping = groupingAssignmentService.setGroupingAttributes(new Grouping(GROUPING));
        assertNotNull(grouping);
        assertNotNull(grouping.getSyncDestinations());
    }

    @Test
    public void groupingOwners() {
        updateMemberService.removeOwnership(ADMIN, GROUPING, TEST_USERNAMES.get(0));
        GroupingsGroupMembers groupingsGroupMembers = groupingAssignmentService.groupingOwners(ADMIN, GROUPING);
        assertNotNull(groupingsGroupMembers);
        assertFalse(groupingsGroupMembers.getGroupMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(TEST_USERNAMES.get(0))));

        updateMemberService.addOwnership(ADMIN, GROUPING, TEST_USERNAMES.get(0));
        groupingsGroupMembers = groupingAssignmentService.groupingOwners(ADMIN, GROUPING);
        assertNotNull(groupingsGroupMembers);
        assertTrue(groupingsGroupMembers.getGroupMembers().stream()
                .anyMatch(groupingsGroupMember -> groupingsGroupMember.getUid().equals(TEST_USERNAMES.get(0))));
        updateMemberService.removeOwnership(ADMIN, GROUPING, TEST_USERNAMES.get(0));
    }

    @Test
    public void isSoleOwner() {
        updateMemberService.addOwnership(ADMIN, GROUPING, ADMIN);
        updateMemberService.addOwnership(ADMIN, GROUPING, TEST_USERNAMES.get(0));
        assertFalse(groupingAssignmentService.isSoleOwner(ADMIN, GROUPING, ADMIN));
        updateMemberService.removeOwnership(ADMIN, GROUPING, TEST_USERNAMES.get(0));
    }

}