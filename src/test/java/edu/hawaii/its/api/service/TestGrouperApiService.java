package edu.hawaii.its.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGrouperApiService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> TEST_NUMBERS;

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    @Autowired
    GrouperApiService grouperApiService;

    @Autowired
    MemberAttributeService memberAttributeService;

    @Autowired
    MembershipService membershipService;

    @Autowired
    private MemberService memberService;

    @BeforeAll
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
        TEST_NUMBERS.forEach(testUhNumber -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUhNumber);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUhNumber);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUhNumber);
            grouperApiService.removeMember(GROUPING_OWNERS, testUhNumber);

            assertFalse(memberService.isOwner(GROUPING, testUhNumber));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUhNumber));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUhNumber));
            assertFalse(memberService.isAdmin(testUhNumber));
        });
    }

    @Test
    public void hasMemberResult() {
        HasMembersResults hasMembersResults = grouperApiService.hasMemberResults(GROUPING_INCLUDE, ADMIN);
        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        List<HasMemberResult> memberResults = hasMembersResults.getResults();
        assertEquals(1, memberResults.size());
        assertEquals(ADMIN, memberResults.get(0).getUid());
    }

    @Test
    public void groupSaveResults() {
        GroupSaveResults groupSaveResults = grouperApiService.groupSaveResults(GROUPING, "Many Groupings in Basis.");
        String description = grouperApiService.findGroupsResults(GROUPING).getGroup().getDescription();
        groupSaveResults = grouperApiService.groupSaveResults(GROUPING, "description");
        assertNotNull(groupSaveResults);
        assertEquals("SUCCESS_UPDATED", groupSaveResults.getResultCode());
        assertEquals("description", grouperApiService.findGroupsResults(GROUPING).getGroup().getDescription());
        groupSaveResults = grouperApiService.groupSaveResults(GROUPING, description);
        assertEquals("SUCCESS_UPDATED", groupSaveResults.getResultCode());
        assertEquals(description, grouperApiService.findGroupsResults(GROUPING).getGroup().getDescription());
        groupSaveResults = grouperApiService.groupSaveResults(GROUPING, description);
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", groupSaveResults.getResultCode());

        groupSaveResults = grouperApiService.groupSaveResults("invalid-path", description);
        assertNull(groupSaveResults); // Todo exception handler.
    }

    @Test
    public void findGroupsResults() {
        FindGroupsResults findGroupsResults = grouperApiService.findGroupsResults(GROUPING_INCLUDE);
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(1, findGroupsResults.getGroups().size());

        findGroupsResults = grouperApiService.findGroupsResults(getGroupPaths());
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(2, findGroupsResults.getGroups().size());

        findGroupsResults = grouperApiService.findGroupsResults("invalid-path");
        assertNotNull(findGroupsResults);
        assertEquals("FAILURE", findGroupsResults.getResultCode());
        assertEquals(0, findGroupsResults.getGroups().size());

        List<String> containsInvalidPath = getGroupPaths();
        containsInvalidPath.add("invalid-path");
        findGroupsResults = grouperApiService.findGroupsResults(containsInvalidPath);
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(2, findGroupsResults.getGroups().size());
    }

    @Test
    public void getSubject() {
        SubjectsResults subjectsResults = grouperApiService.getSubjects(TEST_USERNAMES.get(0));
        List<Subject> subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUCCESS", subject.getResultCode());
        assertEquals(TEST_USERNAMES.get(0), subject.getUid());
        assertEquals(TEST_NUMBERS.get(0), subject.getUhUuid());

        subjectsResults = grouperApiService.getSubjects(TEST_NUMBERS.get(0));
        subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUCCESS", subject.getResultCode());
        assertEquals(TEST_USERNAMES.get(0), subject.getUid());
        assertEquals(TEST_NUMBERS.get(0), subject.getUhUuid());

        subjectsResults = grouperApiService.getSubjects("invalid-identifier");
        subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUBJECT_NOT_FOUND", subject.getResultCode());
    }

    @Test
    public void getSubjects() {
        // Usernames
        SubjectsResults subjectsResults = grouperApiService.getSubjects(TEST_USERNAMES);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(5, subjects.size());
        for (int i = 0; i < 5; i++) {
            Subject subject = subjects.get(i);
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertEquals(TEST_USERNAMES.get(i), subject.getUid());
            assertEquals(TEST_NUMBERS.get(i), subject.getUhUuid());
        }

        // Numbers
        subjectsResults = grouperApiService.getSubjects(TEST_NUMBERS);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(5, subjects.size());
        for (int i = 0; i < 5; i++) {
            Subject subject = subjects.get(i);
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertEquals(TEST_USERNAMES.get(i), subject.getUid());
            assertEquals(TEST_NUMBERS.get(i), subject.getUhUuid());
        }

        // Numbers and Usernames
        List<String> numbersAndUsernames = new ArrayList<>();
        numbersAndUsernames.add(TEST_NUMBERS.get(0));
        numbersAndUsernames.add(TEST_USERNAMES.get(1));
        subjectsResults = grouperApiService.getSubjects(numbersAndUsernames);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(2, subjects.size());
        for (int i = 0; i < 2; i++) {
            Subject subject = subjects.get(i);
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertEquals(TEST_USERNAMES.get(i), subject.getUid());
            assertEquals(TEST_NUMBERS.get(i), subject.getUhUuid());
        }

        // With invalid identifier
        numbersAndUsernames.add("invalid-identifier");
        subjectsResults = grouperApiService.getSubjects(numbersAndUsernames);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(3, subjects.size());
        assertTrue(subjects.stream().anyMatch(subject -> subject.getResultCode().equals("SUBJECT_NOT_FOUND")));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUhUuid().equals(TEST_NUMBERS.get(0))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUhUuid().equals(TEST_NUMBERS.get(1))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUid().equals(TEST_USERNAMES.get(0))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUid().equals(TEST_USERNAMES.get(1))));

        // All invalid identifiers.
        List<String> invalidIdentifiers = new ArrayList<>();
        invalidIdentifiers.add("invalid-identifier-0");
        invalidIdentifiers.add("invalid-identifier-1");
        subjectsResults = grouperApiService.getSubjects(invalidIdentifiers);
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(1, subjects.size());
        assertEquals("SUBJECT_NOT_FOUND", subjects.get(0).getResultCode());
    }

    @Test
    public void groupAttributeResults() {
        String optIn = OptType.IN.value();
        String optOut = OptType.OUT.value();
        List<String> attributes = Arrays.asList(optIn, optOut);

        GroupAttributeResults groupAttributeResults = grouperApiService.groupAttributeResults(TRIO);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());
        assertNotNull(groupAttributeResults);

        groupAttributeResults = grouperApiService.groupAttributeResults(attributes);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());
        assertNotNull(groupAttributeResults);

        groupAttributeResults = grouperApiService.groupAttributeResults(optIn, GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperApiService.groupAttributeResults(optIn, getGroupPaths());
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperApiService.groupAttributeResults(attributes, GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperApiService.groupAttributeResults(attributes, getGroupPaths());
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperApiService.groupAttributeResult(GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperApiService.groupAttributeResult("invalid-path");
        assertNull(groupAttributeResults); // Todo exception handler.

        groupAttributeResults = grouperApiService.groupAttributeResults("invalid-attribute");
        assertNull(groupAttributeResults); // Todo exception handler.

    }

    @Test
    public void getGroupsResults() {
        GetGroupsResults getGroupsResults = grouperApiService.getGroupsResults(ADMIN);
        assertNotNull(getGroupsResults);
        assertEquals("SUCCESS", getGroupsResults.getResultCode());

        String validQuery = "tmp";
        getGroupsResults = grouperApiService.getGroupsResults(ADMIN, validQuery);
        assertNotNull(getGroupsResults);
        assertEquals("SUCCESS", getGroupsResults.getResultCode());
        List<Group> groups = getGroupsResults.getGroups();
        assertFalse(groups.isEmpty());
        assertTrue(groups.stream().allMatch(group -> group.getGroupPath().startsWith(validQuery)));

        String invalidQuery = "there-is-no-way-any-grouping-path-contains-this-string";
        getGroupsResults = grouperApiService.getGroupsResults(ADMIN, invalidQuery);
    }

    @Test
    public void getMembersResult() {
        GetMembersResult getMembersResult = grouperApiService.getMembersResult(GROUPING);
        assertNotNull(getMembersResult);
        assertEquals("SUCCESS", getMembersResult.getResultCode());
        // Todo exception handler for invalid paths.
    }

    @Test
    public void getMembersResults() {
        GetMembersResults getMembersResults = grouperApiService.getMembersResults(getGroupPaths());
        assertNotNull(getMembersResults);
        assertEquals("SUCCESS", getMembersResults.getResultCode());

        List<String> containsInvalidPath = getGroupPaths();
        containsInvalidPath.add("invalid-path");
        getMembersResults = grouperApiService.getMembersResults(getGroupPaths());
        assertNotNull(getMembersResults);
        assertEquals("SUCCESS", getMembersResults.getResultCode());

        getMembersResults = grouperApiService.getMembersResults(Arrays.asList("invalid-path"));
        assertNull(getMembersResults); // Todo exception handler.

    }

    @Test
    public void findAttributesResults() {
        FindAttributesResults findAttributesResults =
                grouperApiService.findAttributesResults(SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);
        assertNotNull(findAttributesResults);
        assertEquals("SUCCESS", findAttributesResults.getResultCode());

        findAttributesResults =
                grouperApiService.findAttributesResults("invalid-attr-type-name", SYNC_DESTINATIONS_LOCATION);
        assertNull(findAttributesResults); // Todo exception handler.

        findAttributesResults =
                grouperApiService.findAttributesResults(SYNC_DESTINATIONS_CHECKBOXES, "invalid-scope-loc");
        assertEquals("FAILURE", findAttributesResults.getResultCode());

        findAttributesResults =
                grouperApiService.findAttributesResults("invalid-attr-type-name", "invalid-scope-loc");
        assertNull(findAttributesResults); // Todo exception handler.

    }

    @Test
    public void addMemberTest() {
        // With uh usernames.
        AddMemberResult addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0)));

        addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1)));
        //// Clean up
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));

        // With uh numbers.
        addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0)));

        addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1)));
        //// Clean up
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1));
    }

    @Test
    public void addRemoveMembers() {
        // With usernames.
        AddMembersResults addMembersResults = grouperApiService.addMembers(GROUPING_INCLUDE, TEST_USERNAMES);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        for (String username : TEST_USERNAMES) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, username));
        }
        RemoveMembersResults removeMembersResults = grouperApiService.removeMembers(GROUPING_INCLUDE, TEST_USERNAMES);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        for (String username : TEST_USERNAMES) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, username));
        }

        // With numbers.
        addMembersResults = grouperApiService.addMembers(GROUPING_INCLUDE, TEST_NUMBERS);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        for (String number : TEST_NUMBERS) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, number));
        }
        removeMembersResults = grouperApiService.removeMembers(GROUPING_INCLUDE, TEST_NUMBERS);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        for (String number : TEST_NUMBERS) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, number));
        }

        // Invalid identifiers.
        addMembersResults = grouperApiService.addMembers(GROUPING_INCLUDE, Arrays.asList("invalid-identifier"));
        assertNull(addMembersResults); // Todo exception handler
        removeMembersResults = grouperApiService.removeMembers(GROUPING_INCLUDE, Arrays.asList("invalidIdentifier"));
        /*
        assertNull(removeMembersResults); // Todo exception handler
         */

        addMembersResults = grouperApiService.addMembers("invalid-path", TEST_USERNAMES);
        assertNull(addMembersResults); // Todo exception handler
        removeMembersResults = grouperApiService.removeMembers("invalid-path", TEST_USERNAMES);
        assertNull(removeMembersResults); // Todo exception handler

    }

    @Test
    public void removeMember() {
        // With uh usernames.
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        RemoveMemberResult removeMemberResult =
                grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0)));
        removeMemberResult = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1)));

        // With uh numbers.
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1));
        removeMemberResult = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_NUMBERS.get(0)));
        removeMemberResult = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_NUMBERS.get(1)));
    }

    @Test
    public void resetGroupMembers() {
        grouperApiService.addMembers(GROUPING_INCLUDE, TEST_USERNAMES);
        GetMembersResult getMembersResult = grouperApiService.getMembersResult(GROUPING_INCLUDE);
        List<String> uhUuids =
                getMembersResult.getSubjects().stream().map(Subject::getUhUuid).collect(Collectors.toList());
        AddMembersResults resetResults = grouperApiService.resetGroupMembers(GROUPING_INCLUDE);
        assertNotNull(resetResults);
        assertEquals("SUCCESS", resetResults.getResultCode());
        assertTrue(grouperApiService.getMembersResult(GROUPING_INCLUDE).getSubjects().isEmpty());
        grouperApiService.addMembers(GROUPING_INCLUDE, uhUuids);
        grouperApiService.removeMembers(GROUPING_INCLUDE, TEST_USERNAMES);

        resetResults = grouperApiService.resetGroupMembers("invalid-path");
        assertNull(resetResults); // Todo exception handler
    }

    @Test
    public void assignAttributesResults() {
        AssignAttributesResults assignAttributesResults =
                grouperApiService.assignAttributesResults(ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        OptType.IN.value());
        assertEquals("SUCCESS", assignAttributesResults.getResultCode());
        assertEquals(OptType.IN.value(), assignAttributesResults.getAttributesResults().get(0).getName());

        assignAttributesResults =
                grouperApiService.assignAttributesResults("invalid_assign_type", OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperApiService.assignAttributesResults(ASSIGN_TYPE_GROUP, "invalid-operation", GROUPING,
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperApiService.assignAttributesResults(ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, "invalid-path",
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperApiService.assignAttributesResults(ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        "invalid-attribute");
        assertNull(assignAttributesResults); // Todo exception handler
    }

    @Test
    public void grouperPrivilegesLiteResultTest() {
        WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLiteResult =
                grouperApiService.assignGrouperPrivilegesLiteResult(GROUPING, PrivilegeType.IN.value(),
                        grouperApiService.subjectLookup(ADMIN), true);
        assertNotNull(assignGrouperPrivilegesLiteResult);
    }

    @Test
    public void subjectLookupTest() {
        for (String testUsername : TEST_USERNAMES) {
            WsSubjectLookup subjectLookup = grouperApiService.subjectLookup(testUsername);
            assertEquals(testUsername, subjectLookup.getSubjectIdentifier());
            assertNull(subjectLookup.getSubjectId());
            assertNull(subjectLookup.getSubjectSourceId());
        }
        for (String testUhNumber : TEST_NUMBERS) {
            WsSubjectLookup subjectLookup = grouperApiService.subjectLookup(testUhNumber);
            assertEquals(testUhNumber, subjectLookup.getSubjectId());
            assertNull(subjectLookup.getSubjectIdentifier());
            assertNull(subjectLookup.getSubjectSourceId());
        }
    }

    /**
     * A list containing two group paths.
     */
    private List<String> getGroupPaths() {
        List<String> list = new ArrayList<>();
        list.add(GROUPING_INCLUDE);
        list.add(GROUPING_EXCLUDE);
        return list;
    }

}
