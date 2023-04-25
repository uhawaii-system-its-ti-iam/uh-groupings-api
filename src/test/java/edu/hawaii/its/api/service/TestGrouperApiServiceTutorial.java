package edu.hawaii.its.api.service;
/**
 * DISCLAIMER:
 * This is NOT a normal integration test.
 * This is a learning tool that is used to help better understand the Grouper API.
 * <p>
 * These tests below do not provide any extra code coverage for our project, but they
 * provide a guide as to how each Grouper API function works and what should be expected of them.
 * Although the functions in GrouperApiService use grouper client (Gc) and web service (Ws)
 * files that were not written by us, the functions itself are used in other places throughout
 * the API, so it will also be beneficial if you look through how these functions are used in
 * the context of our code as well. There is also documentation on how each Gc and Ws function
 * works here: https://spaces.at.internet2.edu/display/Grouper/Grouper+Web+Services
 */

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGrouperApiServiceTutorial {
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

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

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
        TEST_UH_NUMBERS.forEach(testUhNumber -> {
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
        addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0)));

        addMemberResult = grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        assertNotNull(addMemberResult);
        assertTrue(memberService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1)));
        //// Clean up
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
    }

    @Test
    public void removeMemberTest() {
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
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        removeMemberResult = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0)));
        removeMemberResult = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        assertNotNull(removeMemberResult);
        assertFalse(memberService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1)));
    }

    @Test
    public void groupAttributeResults() {
        // All group with these attributes
        GroupAttributeResults groupAttributeResults =
                grouperApiService.groupAttributeResults(Arrays.asList(TRIO, OptType.IN.value()));
        assertNotNull(groupAttributeResults);
        List<GroupAttribute> groupAttributes = groupAttributeResults.getGroupAttributes();
        List<AttributesResult> attributesResults = groupAttributeResults.getAttributesResults();
        groupAttributes.forEach(Assertions::assertNotNull);
        attributesResults.forEach(Assertions::assertNotNull);
        assertEquals(attributesResults.size(), 2);
        attributesResults.forEach(
                defName -> assertTrue(defName.getName().equals(TRIO) || defName.getName().equals(OptType.IN.value())));
        groupAttributes.forEach(groupAttribute -> assertEquals(ASSIGN_TYPE_GROUP, groupAttribute.getAssignType()));
        groupAttributes.forEach(groupAttribute -> assertTrue(groupAttribute.getAttributeName().equals(TRIO) ||
                groupAttribute.getAttributeName().equals(OptType.IN.value())));

        // Attributes of a single grouping.
        groupAttributeResults = grouperApiService.groupAttributeResult(GROUPING);
        assertNotNull(groupAttributes);
        groupAttributes = groupAttributeResults.getGroupAttributes();
        attributesResults = groupAttributeResults.getAttributesResults();
        groupAttributes.forEach(Assertions::assertNotNull);
        attributesResults.forEach(Assertions::assertNotNull);
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getGroupPath(), GROUPING));

        // All Groups and there attributes.
        groupAttributeResults = grouperApiService.groupAttributeResults(TRIO);
        assertNotNull(groupAttributeResults);
        List<Group> groups = groupAttributeResults.getGroups();
        groupAttributes = groupAttributeResults.getGroupAttributes();
        attributesResults = groupAttributeResults.getAttributesResults();
        groupAttributes.forEach(Assertions::assertNotNull);
        attributesResults.forEach(Assertions::assertNotNull);
        groups.forEach(Assertions::assertNotNull);
        assertEquals(attributesResults.size(), 1);
        assertEquals(groupAttributes.get(0).getAttributeName(), TRIO);
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAttributeName(), TRIO));

        // Check if a group has a certain attribute.
        groupAttributeResults = grouperApiService.groupAttributeResults(TRIO, GROUPING);
        assertNotNull(groupAttributeResults);
        groupAttributes = groupAttributeResults.getGroupAttributes();
        attributesResults = groupAttributeResults.getAttributesResults();
        groupAttributes.forEach(Assertions::assertNotNull);
        attributesResults.forEach(Assertions::assertNotNull);
        assertEquals(attributesResults.size(), 1);
        assertEquals(attributesResults.get(0).getName(), TRIO);
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAttributeName(), TRIO));
        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getGroupPath(), GROUPING));
    }

    @Test
    public void hasMemberResultsTest() {
        // Using uh numbers (one that is a member)
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        HasMembersResults hasMemberResultsIsMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(hasMemberResultsIsMember);
        List<HasMemberResult> memberResultsIsMember = hasMemberResultsIsMember.getResults();
        assertEquals(hasMemberResultsIsMember.getGroupPath(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getUhUuid(), TEST_UH_NUMBERS.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultCode(), "IS_MEMBER");
        // Using uh numbers (one that is not a member)
        HasMembersResults hasMemberResultsNonMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        assertNotNull(hasMemberResultsNonMember);
        List<HasMemberResult> memberResultsNonMember = hasMemberResultsNonMember.getResults();
        assertEquals(hasMemberResultsNonMember.getGroupPath(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getUhUuid(), TEST_UH_NUMBERS.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultCode(), "IS_NOT_MEMBER");

        // Using uh usernames (one that is a member)
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        hasMemberResultsIsMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(hasMemberResultsIsMember);
        memberResultsIsMember = hasMemberResultsIsMember.getResults();
        assertEquals(hasMemberResultsIsMember.getGroupPath(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getUid(), TEST_USERNAMES.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultCode(), "IS_MEMBER");
        // Using uh usernames (one that is not a member)
        hasMemberResultsNonMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        assertNotNull(hasMemberResultsNonMember);
        memberResultsNonMember = hasMemberResultsNonMember.getResults();
        assertEquals(hasMemberResultsNonMember.getGroupPath(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getUid(), TEST_USERNAMES.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultCode(), "IS_NOT_MEMBER");

        // cleanup
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
    }

    @Test
    public void assignGrouperPrivilegesLiteResult() {
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
                grouperApiService.assignGrouperPrivilegesResult(GROUPING, PrivilegeType.IN.value(), ADMIN, true);
        assertNotNull(assignGrouperPrivilegesResult);
        Group group = assignGrouperPrivilegesResult.getGroup();
        assertNotNull(group);
        assertEquals(GROUPING, group.getGroupPath());
        assertEquals(PrivilegeType.IN.value(), assignGrouperPrivilegesResult.getPrivilegeName());
        assertEquals("access", assignGrouperPrivilegesResult.getPrivilegeType());
        assertEquals(ADMIN, assignGrouperPrivilegesResult.getSubject().getUid());
    }

    @Test
    public void membersResultsTest() {
        WsGetMembersResults membersResults = grouperApiService.membersResults(
                ASSIGN_TYPE_GROUP,
                grouperApiService.subjectLookup(ADMIN),
                Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE),
                null,
                null,
                "name",
                true
        );
        assertNotNull(membersResults);
        List<WsGetMembersResult> getMembersResult = Arrays.asList(membersResults.getResults());
        List<String> subjectAttributeNames = Arrays.asList(membersResults.getSubjectAttributeNames());

        assertEquals(getMembersResult.size(), 2);
        getMembersResult.forEach(
                results -> assertTrue(results.getWsGroup().getName().equals(GROUPING_INCLUDE) ||
                        results.getWsGroup().getName().equals(GROUPING_EXCLUDE)));
        assertEquals(subjectAttributeNames.get(0), ASSIGN_TYPE_GROUP);
    }

    @Test
    public void groupsResultsTest() {
        GetGroupsResults getGroupsResultsUserNames = grouperApiService.getGroupsResults(TEST_USERNAMES.get(0));
        GetGroupsResults getGroupsResultsNumbers = grouperApiService.getGroupsResults(TEST_UH_NUMBERS.get(0));
        assertNotNull(getGroupsResultsNumbers);
        assertFalse(getGroupsResultsNumbers.getGroups().isEmpty());
        assertEquals(TEST_UH_NUMBERS.get(0), getGroupsResultsNumbers.getSubject().getUhUuid());

        assertNotNull(getGroupsResultsUserNames);
        assertFalse(getGroupsResultsUserNames.getGroups().isEmpty());
        assertEquals(TEST_USERNAMES.get(0), getGroupsResultsUserNames.getSubject().getUid());

    }

    @Test
    public void subjectsResultsTest() {
        SubjectsResults subjectsResults = grouperApiService.getSubjects(ADMIN);
        List<Subject> subjects = subjectsResults.getSubjects();
        assertEquals(1, subjects.size());
        assertEquals(ADMIN, subjects.get(0).getUid());
    }

}