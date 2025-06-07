package edu.hawaii.its.api.service;

import static com.jayway.jsonpath.internal.function.ParamType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
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

import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGrouperApiService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_single}")
    private String GROUPING_SINGLE;

    @Value("${groupings.api.test.grouping_single_owners}")
    private String GROUPING_SINGLE_OWNERS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.test.grouping_large_basis}")
    private String GROUPING_LARGE_BASIS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    private List<String> testUids;
    private List<String> testUhUuids;

    @Autowired
    private GrouperService grouperService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    @BeforeAll
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));

        GroupingMembers testGroupingMembers = uhIdentifierGenerator.getRandomMembers(5);
        testUids = testGroupingMembers.getUids();
        testUhUuids = testGroupingMembers.getUhUuids();

        testUids.forEach(testUid -> {
            grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
            grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
            grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);

            assertFalse(memberService.isOwner(GROUPING, testUid));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
            assertFalse(memberService.isAdmin(testUid));
        });
    }

    @Test
    public void hasMemberResults() {
        HasMembersResults hasMembersResults = grouperService.hasMemberResults(GROUPING_INCLUDE, ADMIN);
        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        List<HasMemberResult> memberResults = hasMembersResults.getResults();
        assertEquals(1, memberResults.size());
        assertEquals(ADMIN, memberResults.get(0).getUid());
    }

    @Test
    public void hasMembersResults() {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(ADMIN, GROUPING_INCLUDE, testUids);
        assertNotNull(hasMembersResults);
        assertEquals("SUCCESS", hasMembersResults.getResultCode());
        List<HasMemberResult> memberResults = hasMembersResults.getResults();
        assertEquals(testUids.size(), memberResults.size());
        assertTrue(IntStream.range(0, testUids.size())
                .allMatch(i -> testUids.get(i).equals(memberResults.get(i).getUid())));
    }

    @Test
    public void groupSaveResults() {
        GroupSaveResults groupSaveResults = grouperService.groupSaveResults(GROUPING, "Many Groupings in Basis.");
        String description = grouperService.findGroupsResults(GROUPING).getGroup().getDescription();
        groupSaveResults = grouperService.groupSaveResults(GROUPING, "description");
        assertNotNull(groupSaveResults);
        assertEquals("SUCCESS_UPDATED", groupSaveResults.getResultCode());
        assertEquals("description", grouperService.findGroupsResults(GROUPING).getGroup().getDescription());
        groupSaveResults = grouperService.groupSaveResults(GROUPING, description);
        assertEquals("SUCCESS_UPDATED", groupSaveResults.getResultCode());
        assertEquals(description, grouperService.findGroupsResults(GROUPING).getGroup().getDescription());
        groupSaveResults = grouperService.groupSaveResults(GROUPING, description);
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", groupSaveResults.getResultCode());

        groupSaveResults = grouperService.groupSaveResults("invalid-path", description);
        assertNull(groupSaveResults); // Todo exception handler.
    }

    @Test
    public void findGroupsResults() {
        FindGroupsResults findGroupsResults = grouperService.findGroupsResults(GROUPING_INCLUDE);
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(1, findGroupsResults.getGroups().size());

        findGroupsResults = grouperService.findGroupsResults(getGroupPaths());
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(2, findGroupsResults.getGroups().size());

        findGroupsResults = grouperService.findGroupsResults("invalid-path");
        assertNotNull(findGroupsResults);
        assertEquals("FAILURE", findGroupsResults.getResultCode());
        assertEquals(0, findGroupsResults.getGroups().size());

        List<String> containsInvalidPath = getGroupPaths();
        containsInvalidPath.add("invalid-path");
        findGroupsResults = grouperService.findGroupsResults(containsInvalidPath);
        assertNotNull(findGroupsResults);
        assertEquals("SUCCESS", findGroupsResults.getResultCode());
        assertEquals(2, findGroupsResults.getGroups().size());
    }

    @Test
    public void getSubject() {
        SubjectsResults subjectsResults = grouperService.getSubjects(testUids.get(0));
        List<Subject> subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUCCESS", subject.getResultCode());
        assertEquals(testUids.get(0), subject.getUid());
        assertEquals(testUhUuids.get(0), subject.getUhUuid());

        subjectsResults = grouperService.getSubjects(testUhUuids.get(0));
        subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUCCESS", subject.getResultCode());
        assertEquals(testUids.get(0), subject.getUid());
        assertEquals(testUhUuids.get(0), subject.getUhUuid());

        subjectsResults = grouperService.getSubjects("invalid-identifier");
        subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        subject = subjects.get(0);
        assertNotNull(subject);
        assertEquals("SUBJECT_NOT_FOUND", subject.getResultCode());
    }

    @Test
    public void getSubjects() {
        HashSet<String> testUidsSet = new HashSet<>(testUids);
        HashSet<String> testUhUuidsSet = new HashSet<>(testUhUuids);

        // Uids
        SubjectsResults subjectsResults = grouperService.getSubjects(testUids);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        List<Subject> subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(5, subjects.size());
        for (Subject subject : subjects) {
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertTrue(testUidsSet.contains(subject.getUid()));
            assertTrue(testUhUuidsSet.contains(subject.getUhUuid()));
        }

        // Numbers
        subjectsResults = grouperService.getSubjects(testUhUuids);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(5, subjects.size());
        for (int i = 0; i < 5; i++) {
            Subject subject = subjects.get(i);
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertTrue(testUidsSet.contains(subject.getUid()));
            assertTrue(testUhUuidsSet.contains(subject.getUhUuid()));
        }

        List<String> uhUuidsAndUids = new ArrayList<>();
        uhUuidsAndUids.add(testUhUuids.get(0));
        uhUuidsAndUids.add(testUids.get(1));
        subjectsResults = grouperService.getSubjects(uhUuidsAndUids);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(2, subjects.size());
        for (int i = 0; i < 2; i++) {
            Subject subject = subjects.get(i);
            assertNotNull(subject);
            assertEquals("SUCCESS", subject.getResultCode());
            assertTrue(testUidsSet.contains(subject.getUid()));
            assertTrue(testUhUuidsSet.contains(subject.getUhUuid()));
        }

        // With invalid identifier
        uhUuidsAndUids.add("invalid-identifier");
        subjectsResults = grouperService.getSubjects(uhUuidsAndUids);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(3, subjects.size());
        assertTrue(subjects.stream().anyMatch(subject -> subject.getResultCode().equals("SUBJECT_NOT_FOUND")));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUhUuid().equals(testUhUuids.get(0))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUhUuid().equals(testUhUuids.get(1))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUid().equals(testUids.get(0))));
        assertTrue(subjects.stream().anyMatch(subject -> subject.getUid().equals(testUids.get(1))));

        // All invalid identifiers.
        List<String> invalidIdentifiers = new ArrayList<>();
        invalidIdentifiers.add("invalid-identifier-0");
        invalidIdentifiers.add("invalid-identifier-1");
        subjectsResults = grouperService.getSubjects(invalidIdentifiers);
        assertNotNull(subjectsResults);
        assertEquals("FAILURE", subjectsResults.getResultCode());
        subjects = subjectsResults.getSubjects();
        assertNotNull(subjects);
        assertEquals(1, subjects.size());
        assertEquals("SUBJECT_NOT_FOUND", subjects.get(0).getResultCode());
    }

    @Test
    public void getImmediateMembers() {
        grouperService.addGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING_SINGLE));
        GetMembersResult immediateMembers = grouperService.getImmediateMembers(ADMIN, GROUPING_OWNERS);
        assertNotNull(immediateMembers);
        assertEquals("SUCCESS", immediateMembers.getResultCode());
        SubjectsResults subjectsResults = grouperService.getSubjects(GROUPING_OWNERS, GROUPING_SINGLE);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        grouperService.removeGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING_SINGLE));
    }

    @Test
    public void getAllMembers() {
        grouperService.addGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING_SINGLE));
        GetMembersResult allMembers = grouperService.getAllMembers(ADMIN, GROUPING_OWNERS);
        assertNotNull(allMembers);
        assertEquals("SUCCESS", allMembers.getResultCode());
        SubjectsResults subjectsResults = grouperService.getSubjects(GROUPING_OWNERS, GROUPING_SINGLE);
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
        grouperService.removeGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING_SINGLE));
    }

    @Test
    public void groupAttributeResults() {
        String optIn = OptType.IN.value();
        String optOut = OptType.OUT.value();
        List<String> attributes = Arrays.asList(optIn, optOut);

        GroupAttributeResults groupAttributeResults = grouperService.groupAttributeResults(TRIO);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());
        assertNotNull(groupAttributeResults);

        groupAttributeResults = grouperService.groupAttributeResults(attributes);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());
        assertNotNull(groupAttributeResults);

        groupAttributeResults = grouperService.groupAttributeResults(optIn, GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperService.groupAttributeResults(optIn, getGroupPaths());
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperService.groupAttributeResults(attributes, GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperService.groupAttributeResults(attributes, getGroupPaths());
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperService.groupAttributeResult(GROUPING);
        assertNotNull(groupAttributeResults);
        assertEquals("SUCCESS", groupAttributeResults.getResultCode());

        groupAttributeResults = grouperService.groupAttributeResult("invalid-path");
        assertNull(groupAttributeResults); // Todo exception handler.

        groupAttributeResults = grouperService.groupAttributeResults("invalid-attribute");
        assertNull(groupAttributeResults); // Todo exception handler.

    }

    @Test
    public void getGroupsResults() {
        GetGroupsResults getGroupsResults = grouperService.getGroupsResults(ADMIN);
        assertNotNull(getGroupsResults);
        assertEquals("SUCCESS", getGroupsResults.getResultCode());

        String validQuery = "tmp";
        getGroupsResults = grouperService.getGroupsResults(ADMIN, validQuery);
        assertNotNull(getGroupsResults);
        assertEquals("SUCCESS", getGroupsResults.getResultCode());
        List<Group> groups = getGroupsResults.getGroups();
        assertFalse(groups.isEmpty());
        assertTrue(groups.stream().allMatch(group -> group.getGroupPath().startsWith(validQuery)));

        String invalidQuery = "there-is-no-way-any-grouping-path-contains-this-string";
        getGroupsResults = grouperService.getGroupsResults(ADMIN, invalidQuery);
    }

    @Test
    public void getMembersResult() {
        GetMembersResult getMembersResult = grouperService.getMembersResult(ADMIN, GROUPING);
        assertNotNull(getMembersResult);
        assertEquals("SUCCESS", getMembersResult.getResultCode());
        // Todo exception handler for invalid paths.

        getMembersResult = grouperService.getMembersResult(
                ADMIN,
                GROUPING_LARGE_BASIS,
                1,
                20,
                "name",
                true);
        assertNotNull(getMembersResult);
        assertEquals("SUCCESS", getMembersResult.getResultCode());
        assertEquals(GROUPING_LARGE_BASIS, getMembersResult.getGroup().getGroupPath());
        assertFalse(getMembersResult.getSubjects().isEmpty());
    }

    @Test
    public void getMembersResults() {
        GetMembersResults getMembersResults = grouperService.getMembersResults(getGroupPaths());
        assertNotNull(getMembersResults);
        assertEquals("SUCCESS", getMembersResults.getResultCode());

        List<String> containsInvalidPath = getGroupPaths();
        containsInvalidPath.add("invalid-path");
        getMembersResults = grouperService.getMembersResults(getGroupPaths());
        assertNotNull(getMembersResults);
        assertEquals("SUCCESS", getMembersResults.getResultCode());

        getMembersResults = grouperService.getMembersResults(Arrays.asList("invalid-path"));
        assertNull(getMembersResults); // Todo exception handler.

        getMembersResults = grouperService.getMembersResults(
                ADMIN,
                Arrays.asList(GROUPING_LARGE_BASIS),
                1,
                700,
                "name",
                true);
        assertNotNull(getMembersResults);
        assertEquals("SUCCESS", getMembersResults.getResultCode());
        assertEquals(GROUPING_LARGE_BASIS, getMembersResults.getMembersResults().get(0).getGroup().getGroupPath());
        assertFalse(getMembersResults.getMembersResults().get(0).getSubjects().isEmpty());
    }

    @Test
    public void findAttributesResults() {
        FindAttributesResults findAttributesResults =
                grouperService.findAttributesResults(SYNC_DESTINATIONS_CHECKBOXES, SYNC_DESTINATIONS_LOCATION);
        assertNotNull(findAttributesResults);
        assertEquals("SUCCESS", findAttributesResults.getResultCode());

        findAttributesResults =
                grouperService.findAttributesResults("invalid-attr-type-name", SYNC_DESTINATIONS_LOCATION);
        assertNull(findAttributesResults); // Todo exception handler.

        findAttributesResults =
                grouperService.findAttributesResults(SYNC_DESTINATIONS_CHECKBOXES, "invalid-scope-loc");
        assertEquals("FAILURE", findAttributesResults.getResultCode());

        findAttributesResults =
                grouperService.findAttributesResults("invalid-attr-type-name", "invalid-scope-loc");
        assertNull(findAttributesResults); // Todo exception handler.

    }

    @Test
    public void addMemberTest() {
        // With uh uids.
        AddMemberResult addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUids.get(0)));

        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUids.get(1)));
        //// Clean up
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));

        // With uh numbers.
        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(0)));

        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(1)));
        //// Clean up
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
    }

    @Test
    public void addRemoveMembers() {
        // With uids.
        AddMembersResults addMembersResults = grouperService.addMembers(ADMIN, GROUPING_INCLUDE, testUids);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        for (String uid : testUids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, uid));
        }
        RemoveMembersResults removeMembersResults = grouperService.removeMembers(ADMIN, GROUPING_INCLUDE, testUids);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        for (String uid : testUids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, uid));
        }

        // With numbers.
        addMembersResults = grouperService.addMembers(ADMIN, GROUPING_INCLUDE, testUhUuids);
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        for (String number : testUhUuids) {
            assertTrue(memberService.isMember(GROUPING_INCLUDE, number));
        }

        removeMembersResults = grouperService.removeMembers(ADMIN, GROUPING_INCLUDE, testUhUuids);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        for (String number : testUhUuids) {
            assertFalse(memberService.isMember(GROUPING_INCLUDE, number));
        }

        // Invalid identifiers.
        addMembersResults = grouperService.addMembers(ADMIN, GROUPING_INCLUDE, Arrays.asList("invalid-identifier"));
        assertNull(addMembersResults); // Todo exception handler
        removeMembersResults = grouperService.removeMembers(ADMIN, GROUPING_INCLUDE, Arrays.asList("invalidIdentifier"));
        /* assertNull(removeMembersResults); // Todo exception handler */
        addMembersResults = grouperService.addMembers(ADMIN,"invalid-path", testUids);
        assertNull(addMembersResults); // Todo exception handler
        removeMembersResults = grouperService.removeMembers(ADMIN, "invalid-path", testUids);
        assertNull(removeMembersResults); // Todo exception handler
    }

    @Test
    public void addRemovePathOwners() {
        // With Group Path.
        AddMembersResults addMembersResults = grouperService.addGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING));
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());

        RemoveMembersResults removeMembersResults = grouperService.removeGroupPathOwners(ADMIN, GROUPING_OWNERS, Collections.singletonList(GROUPING));
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());

        // Add and Remove duplicated group path owners
        addMembersResults = grouperService.addGroupPathOwners(ADMIN, GROUPING_OWNERS, Arrays.asList(GROUPING, GROUPING));
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS_ALREADY_EXISTED", addMembersResults.getResults().get(addMembersResults.getResults().size()-1).getResultCode());
        removeMembersResults = grouperService.removeGroupPathOwners(ADMIN, GROUPING_OWNERS, Arrays.asList(GROUPING, GROUPING));
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeMembersResults.getResults().get(removeMembersResults.getResults().size()-1).getResultCode());
    }

    @Test
    public void removeMember() {
        // With uh uids.
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
        RemoveMemberResult removeMemberResult =
                grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUids.get(0)));
        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUids.get(1)));

        // With uh numbers.
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(0)));
        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(1)));
    }

    @Test
    public void resetGroupMembers() {
        grouperService.addMembers(ADMIN, GROUPING_INCLUDE, testUids);
        GetMembersResult getMembersResult = grouperService.getMembersResult(ADMIN, GROUPING_INCLUDE);
        List<String> uhUuids =
                getMembersResult.getSubjects().stream().map(Subject::getUhUuid).collect(Collectors.toList());
        AddMembersResults resetResults = grouperService.resetGroupMembers(GROUPING_INCLUDE);
        assertNotNull(resetResults);
        assertEquals("SUCCESS", resetResults.getResultCode());
        assertTrue(grouperService.getMembersResult(ADMIN, GROUPING_INCLUDE).getSubjects().isEmpty());
        grouperService.addMembers(ADMIN, GROUPING_INCLUDE, uhUuids);
        grouperService.removeMembers(ADMIN, GROUPING_INCLUDE, testUids);
        resetResults = grouperService.resetGroupMembers("invalid-path");
        assertNull(resetResults); // Todo exception handler
    }

    @Test
    public void assignAttributesResults() {
        AssignAttributesResults assignAttributesResults =
                grouperService.assignAttributesResults(ADMIN, ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        OptType.IN.value());
        assertEquals("SUCCESS", assignAttributesResults.getResultCode());
        assertEquals(OptType.IN.value(), assignAttributesResults.getAttributesResults().get(0).getName());

        assignAttributesResults =
                grouperService.assignAttributesResults(ADMIN,"invalid_assign_type", OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperService.assignAttributesResults(ADMIN, ASSIGN_TYPE_GROUP, "invalid-operation", GROUPING,
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperService.assignAttributesResults(ADMIN, ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, "invalid-path",
                        OptType.IN.value());
        assertNull(assignAttributesResults); // Todo exception handler

        assignAttributesResults =
                grouperService.assignAttributesResults(ADMIN, ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, GROUPING,
                        "invalid-attribute");
        assertNull(assignAttributesResults); // Todo exception handler
    }

    @Test
    public void assignGrouperPrivilegesLiteResult() {
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                grouperService.assignGrouperPrivilegesResult(ADMIN, GROUPING, PrivilegeType.IN.value(), ADMIN, true);
        assertNotNull(assignGrouperPrivilegesResult);

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
