package edu.hawaii.its.api.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.Subject;

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
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    public Environment env; // Just for the settings check.

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private Person testPerson;

    @BeforeEach
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUid -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUid);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUid);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUid);
            grouperApiService.removeMember(GROUPING_OWNERS, testUid);

            assertFalse(memberService.isOwner(GROUPING, testUid));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
            assertFalse(memberService.isAdmin(testUid));
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
    public void invalidUhIdentifiersTest() {
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.invalidUhIdentifiers("bogus-owner-admin", null));

        List<String> result = memberAttributeService.invalidUhIdentifiers(ADMIN, TEST_USERNAMES);
        assertNotNull(result);
        assertEquals(new ArrayList(), result);

        String testUid = testPerson.getUsername();
        List<String> testList = new ArrayList<>();
        testList.add(testUid);

        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        List<String> invalidUhIdentifiers = new ArrayList<>();
        invalidUhIdentifiers.add("bogus-user1");
        invalidUhIdentifiers.add("bogus-user2");
        result = memberAttributeService.invalidUhIdentifiers(testUid, invalidUhIdentifiers);
        assertNotNull(result);
        assertEquals(invalidUhIdentifiers, result);
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        updateMemberService.addAdmin(ADMIN, testUid);
        List<String> uhIdentifiers = new ArrayList<>(TEST_USERNAMES);
        uhIdentifiers.add("bogus-user1");
        uhIdentifiers.add("bogus-user2");
        result = memberAttributeService.invalidUhIdentifiers(testUid, uhIdentifiers);
        assertEquals(invalidUhIdentifiers, result);
        updateMemberService.removeAdmin(ADMIN, testUid);
    }

    @Test
    public void memberAttributesTest() {
        TEST_USERNAMES.forEach(testUid -> {
            Person person = memberAttributeService.getMemberAttributes(ADMIN, testUid);
            assertNotNull(person);
            assertEquals(testUid, person.getUsername());
            assertEquals(testUid, person.getUhUuid());
        });

        String testUid = testPerson.getUsername();
        List<String> testList = new ArrayList<>();
        testList.add(testUid);
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
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        person = memberAttributeService.getMemberAttributes(testUid, testUid);
        assertNotNull(person.getName());
        assertNotNull(person.getUhUuid());
        assertNotNull(person.getUsername());
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should not return an empty person if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUid);
        person = memberAttributeService.getMemberAttributes(testUid, testUid);
        assertNotNull(person.getName());
        assertNotNull(person.getUhUuid());
        assertNotNull(person.getUsername());
        updateMemberService.removeAdmin(ADMIN, testUid);

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

        String testUid = testPerson.getUsername();
        List<String> testList = new ArrayList<>();
        testList.add(testUid);

        // Should return an empty array if at least one uhIdentifier is invalid.
        List<String> uhIdentifiers = new ArrayList<>();
        uhIdentifiers.add("bogus-user");
        subjects = memberAttributeService.getMembersAttributes(ADMIN, uhIdentifiers);
        assertEquals(new ArrayList(), subjects);

        // Should throw AccessDeniedException if current user is not an admin or owner.
        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMembersAttributes("bogus-owner-admin", null));

        // Should not return an empty array of subjects if current user is an owner but not an admin.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        subjects = memberAttributeService.getMembersAttributes(testUid, testList);
        assertNotEquals(new ArrayList(), subjects);
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should not return an empty array if current user is an admin but not an owner.
        updateMemberService.addAdmin(ADMIN, testUid);
        subjects = memberAttributeService.getMembersAttributes(testUid, testList);
        assertNotEquals(new ArrayList(), subjects);
        updateMemberService.removeAdmin(ADMIN, testUid);
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
        List<String> testList = new ArrayList<>();
        String testUid = testPerson.getUsername();
        testList.add(testUid);
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, testUid);
        assertFalse(
                groupingsOwned.stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        groupingsOwned = memberAttributeService.getOwnedGroupings(ADMIN, testUid);
        assertTrue(
                groupingsOwned.stream()
                        .anyMatch(groupingPath -> groupingPath.getPath().equals(GROUPING)));

        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);
    }

    @Test
    public void getNumberOfGroupingsTest() {
        String testUid = testPerson.getUsername();
        List<String> testList = new ArrayList<>();
        testList.add(testUid);
        Integer numberOfGroupings = memberAttributeService.numberOfGroupings(ADMIN, testUid);
        assertNotNull(numberOfGroupings);

        // Should equal the size of the list returned from getOwnedGroupings().
        assertEquals(memberAttributeService.getOwnedGroupings(ADMIN, testUid).size(), numberOfGroupings);
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);

        // Should increase by one if user is added as owner to a grouping.
        updateMemberService.addOwnerships(ADMIN, GROUPING, testList);
        assertEquals(numberOfGroupings + 1, memberAttributeService.numberOfGroupings(ADMIN, testUid));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testList);

        // Should decrease by one if user is added as owner to a grouping.
        assertEquals(numberOfGroupings, memberAttributeService.numberOfGroupings(ADMIN, testUid));
    }
}