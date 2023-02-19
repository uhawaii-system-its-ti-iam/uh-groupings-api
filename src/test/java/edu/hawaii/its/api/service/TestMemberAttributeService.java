package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.Subject;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMemberAttributeService {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN_USER;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    private static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

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
    public void isMemberTest() {
        // Should not be members.
        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberAttributeService.isMember(GROUPING_OWNERS, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_OWNERS,
                    new Person(testUsername, testUsername, null)));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE,
                    new Person(testUsername, testUsername, null)));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE,
                    new Person(testUsername, testUsername, testUsername)));
        });

        // Should be members after add.
        membershipService.addIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        membershipService.addOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        TEST_USERNAMES.forEach(testUsername -> {
            assertTrue(memberAttributeService.isMember(GROUPING_OWNERS, testUsername));
            assertTrue(memberAttributeService.isMember(GROUPING_OWNERS,
                    new Person(testUsername, testUsername, null)));
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE,
                    new Person(testUsername, testUsername, null)));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE,
                    new Person(testUsername, testUsername, testUsername)));
        });
        membershipService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);

        // Should return false when invalid uh username is passed.
        assertFalse(memberAttributeService.isMember(GROUPING, "bogus-user"));
        // Should return false when invalid uh number is passed.
        assertFalse(memberAttributeService.isMember(GROUPING, "112312455345234"));
        // Should throw an exception if a bad path is passed.
        try {
            memberAttributeService.isMember("bad-path", "iamtst01");
            fail(" Should throw an exception if a bad path is passed.");
        } catch (GcWebServiceError e) {
            assertTrue(e.getMessage().contains("GROUP_NOT_FOUND"));
        }
    }

    @Test
    public void isOwnerTest() {
        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberAttributeService.isOwner(GROUPING, testUsername));
        });
        membershipService.addOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        TEST_USERNAMES.forEach(testUsername -> {
            assertTrue(memberAttributeService.isOwner(GROUPING, testUsername));
        });
        membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
    }

    @Test
    public void isAdminTest() {
        assertTrue(memberAttributeService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberAttributeService.isAdmin(testUsername));
            membershipService.addAdmin(ADMIN, testUsername);
        });
        TEST_USERNAMES.forEach(testUsername -> {
            assertTrue(memberAttributeService.isAdmin(testUsername));
            membershipService.removeAdmin(ADMIN, testUsername);
        });
    }

    @Test
    public void invalidUhIdentifiersTest() {
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.invalidUhIdentifiers("bogus-owner-admin", null));

        List<String> result = memberAttributeService.invalidUhIdentifiers(ADMIN, TEST_USERNAMES);
        assertNotNull(result);
        assertEquals(new ArrayList(), result);

        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        List<String> invalidUhIdentifiers = new ArrayList<>();
        invalidUhIdentifiers.add("bogus-user1");
        invalidUhIdentifiers.add("bogus-user2");
        result = memberAttributeService.invalidUhIdentifiers(iamtst01, invalidUhIdentifiers);
        assertNotNull(result);
        assertEquals(invalidUhIdentifiers, result);
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        membershipService.addAdmin(ADMIN, iamtst01);
        List<String> uhIdentifiers = new ArrayList<>(TEST_USERNAMES);
        uhIdentifiers.add("bogus-user1");
        uhIdentifiers.add("bogus-user2");
        result = memberAttributeService.invalidUhIdentifiers(iamtst01, uhIdentifiers);
        assertEquals(invalidUhIdentifiers, result);
        membershipService.removeAdmin(ADMIN, iamtst01);
    }

    @Test
    public void memberAttributesTest() {

        TEST_USERNAMES.forEach(testUsername -> {
            Person person = memberAttributeService.getMemberAttributes(ADMIN, testUsername);
            assertNotNull(person);
            assertEquals(testUsername, person.getUsername());
            assertEquals(testUsername, person.getUhUuid());
        });

        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        Person person;

        // Should return an empty person if user identifier is invalid.
        person = memberAttributeService.getMemberAttributes(ADMIN, "bogus-user");
        assertNull(person.getName());
        assertNull(person.getUhUuid());
        assertNull(person.getUsername());

        // Should throw AccessDeniedException if current user is not an admin or owner.
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMemberAttributes("bogus-owner-admin", null));

        // Should not return an empty person if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        person = memberAttributeService.getMemberAttributes(iamtst01, iamtst01);
        assertNotNull(person.getName());
        assertNotNull(person.getUhUuid());
        assertNotNull(person.getUsername());
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not return an empty person if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        person = memberAttributeService.getMemberAttributes(iamtst01, iamtst01);
        assertNotNull(person.getName());
        assertNotNull(person.getUhUuid());
        assertNotNull(person.getUsername());
        membershipService.removeAdmin(ADMIN, iamtst01);

    }

    @Test
    public void membersAttributesTest() {
        List<Subject> subjects = memberAttributeService.getMembersAttributes(ADMIN, TEST_USERNAMES);
        assertNotNull(subjects);
        List<String> subjectsUids = subjects
                .stream()
                .map(subject -> subject.getUid())
                .collect(Collectors.toList());
        assertEquals(TEST_USERNAMES, subjectsUids);
        List<String> subjectsUhUuids = subjects
                .stream()
                .map(subject -> subject.getUhUuid())
                .collect(Collectors.toList());
        assertEquals(TEST_USERNAMES, subjectsUhUuids);

        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);

        // Should return an empty array if at least one uhIdentifier is invalid.
        List<String> uhIdentifiers = new ArrayList<>();
        uhIdentifiers.add("bogus-user");
        subjects = memberAttributeService.getMembersAttributes(ADMIN, uhIdentifiers);
        assertEquals(new ArrayList(), subjects);

        // Should throw AccessDeniedException if current user is not an admin or owner.
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMembersAttributes("bogus-owner-admin", null));

        // Should not return an empty array of subjects if current user is an owner but not an admin.
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        subjects = memberAttributeService.getMembersAttributes(iamtst01, iamtst01List);
        assertNotEquals(new ArrayList(), subjects);
        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);

        // Should not return an empty array if current user is an admin but not an owner.
        membershipService.addAdmin(ADMIN, iamtst01);
        subjects = memberAttributeService.getMembersAttributes(iamtst01, iamtst01List);
        assertNotEquals(new ArrayList(), subjects);
        membershipService.removeAdmin(ADMIN, iamtst01);
    }

    @Test
    public void getOwnedGroupingsTest() {
        // Groupings owned by current admin should complement
        // the list of memberships that the current admin is in.
        List<GroupingPath> groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, ADMIN);
        List<Membership> results = membershipService.managePersonResults(ADMIN, ADMIN);
        assertNotNull(groupingsOwned);
        groupingsOwned.forEach(groupingPath -> {
            assertTrue(
                    results.stream()
                            .anyMatch(membership -> membership
                                    .getPath().equals(groupingPath.getPath())));
        });

        // Should contain grouping path if user is added as and owner.
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN_USER, TEST_USERNAMES.get(0));
        assertFalse(
                groupingsOwned.stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN_USER, TEST_USERNAMES.get(0));
        assertTrue(
                groupingsOwned.stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        membershipService.removeOwnerships(GROUPING, ADMIN, iamtst01List);
    }

    @Test
    public void getNumberOfGroupingsTest() {
        String iamtst01 = TEST_USERNAMES.get(0);
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(iamtst01);
        Integer numberOfGroupings = memberAttributeService.numberOfGroupings(ADMIN_USER, iamtst01);
        assertNotNull(numberOfGroupings);

        // Should equal the size of the list returned from getOwnedGroupings().
        assertEquals(memberAttributeService.getOwnedGroupings(ADMIN_USER, iamtst01).size(), numberOfGroupings);
        membershipService.addOwnerships(GROUPING, ADMIN_USER, iamtst01List);

        // Should increase by one if user is added as owner to a grouping.
        membershipService.addOwnerships(GROUPING, ADMIN_USER, iamtst01List);
        assertEquals(numberOfGroupings + 1, memberAttributeService.numberOfGroupings(ADMIN_USER, iamtst01));
        membershipService.removeOwnerships(GROUPING, ADMIN_USER, iamtst01List);

        // Should decrease by one if user is added as owner to a grouping.
        assertEquals(numberOfGroupings, memberAttributeService.numberOfGroupings(ADMIN_USER, iamtst01));
    }

    /**
     * Helper - getMembershipResultsTest
     * Create a sublist which contains all memberships whose path contains the currentUsers uh username (ADMIN) and
     * who is not in basis.
     */
}