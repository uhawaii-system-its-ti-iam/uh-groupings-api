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
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    public void groupsOfTest() {
        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, TRIO);
        assertNotNull(attributeAssignmentsResults);
        List<WsGroup> groups = Arrays.asList(attributeAssignmentsResults.getWsGroups());
        List<WsAttributeAssign> attributeAssigns = Arrays.asList(attributeAssignmentsResults.getWsAttributeAssigns());
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(attributeAssignmentsResults.getWsAttributeDefNames());

        groups.forEach(Assertions::assertNotNull);
        attributeAssigns.forEach(Assertions::assertNotNull);
        attributeDefNames.forEach(Assertions::assertNotNull);
        assertEquals(attributeDefNames.size(), 1);
        assertEquals(attributeDefNames.get(0).getName(), TRIO);
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getAttributeAssignType(), ASSIGN_TYPE_GROUP));
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getAttributeDefNameName(), TRIO));
    }

    @Test
    public void attributeAssignsTest() {
        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperApiService.attributeAssigns(ASSIGN_TYPE_GROUP, TRIO, OptType.IN.value());
        assertNotNull(attributeAssignmentsResults);
        List<WsAttributeAssign> attributeAssigns = Arrays.asList(attributeAssignmentsResults.getWsAttributeAssigns());
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(attributeAssignmentsResults.getWsAttributeDefNames());

        attributeAssigns.forEach(Assertions::assertNotNull);
        attributeDefNames.forEach(Assertions::assertNotNull);
        assertEquals(attributeDefNames.size(), 2);
        attributeDefNames.forEach(
                defName -> assertTrue(defName.getName().equals(TRIO) || defName.getName().equals(OptType.IN.value())));
        attributeAssigns.forEach(
                assignments -> assertEquals(ASSIGN_TYPE_GROUP, assignments.getAttributeAssignType()));
        attributeAssigns.forEach(
                assignments -> assertTrue(assignments.getAttributeDefNameName().equals(TRIO) ||
                        assignments.getAttributeDefNameName().equals(OptType.IN.value())));
    }

    @Test
    public void groupAttributeDefNamesTest() {
        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperApiService.groupAttributeDefNames(ASSIGN_TYPE_GROUP, GROUPING);
        assertNotNull(attributeAssignmentsResults);
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(attributeAssignmentsResults.getWsAttributeDefNames());
        List<WsAttributeAssign> attributeAssigns =
                Arrays.asList(attributeAssignmentsResults.getWsAttributeAssigns());

        attributeDefNames.forEach(Assertions::assertNotNull);
        attributeAssigns.forEach(Assertions::assertNotNull);
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getAttributeAssignType(), ASSIGN_TYPE_GROUP));
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getOwnerGroupName(), GROUPING));
    }

    @Test
    public void groupAttributeAssignsTest() {
        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperApiService.groupAttributeAssigns(ASSIGN_TYPE_GROUP, TRIO, GROUPING);
        assertNotNull(attributeAssignmentsResults);
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(attributeAssignmentsResults.getWsAttributeDefNames());
        List<WsAttributeAssign> attributeAssigns = Arrays.asList(attributeAssignmentsResults.getWsAttributeAssigns());

        attributeDefNames.forEach(Assertions::assertNotNull);
        attributeAssigns.forEach(Assertions::assertNotNull);
        assertEquals(attributeDefNames.size(), 1);
        assertEquals(attributeDefNames.get(0).getName(), TRIO);
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getAttributeAssignType(), ASSIGN_TYPE_GROUP));
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getAttributeDefNameName(), TRIO));
        attributeAssigns.forEach(assignments -> assertEquals(assignments.getOwnerGroupName(), GROUPING));
    }

    @Test
    public void hasMemberResultsTest() {
        // Using uh numbers (one that is a member)
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        WsHasMemberResults hasMemberResultsIsMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(hasMemberResultsIsMember);
        List<WsHasMemberResult> memberResultsIsMember = Arrays.asList(hasMemberResultsIsMember.getResults());
        assertEquals(hasMemberResultsIsMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getWsSubject().getId(), TEST_UH_NUMBERS.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultMetadata().getResultCode(), "IS_MEMBER");
        // Using uh numbers (one that is not a member)
        WsHasMemberResults hasMemberResultsNonMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        assertNotNull(hasMemberResultsNonMember);
        List<WsHasMemberResult> memberResultsNonMember = Arrays.asList(hasMemberResultsNonMember.getResults());
        assertEquals(hasMemberResultsNonMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getWsSubject().getId(), TEST_UH_NUMBERS.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultMetadata().getResultCode(), "IS_NOT_MEMBER");

        // Using uh usernames (one that is a member)
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        hasMemberResultsIsMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(hasMemberResultsIsMember);
        memberResultsIsMember = Arrays.asList(hasMemberResultsIsMember.getResults());
        assertEquals(hasMemberResultsIsMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getWsSubject().getId(), TEST_USERNAMES.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultMetadata().getResultCode(), "IS_MEMBER");
        // Using uh usernames (one that is not a member)
        hasMemberResultsNonMember =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        assertNotNull(hasMemberResultsNonMember);
        memberResultsNonMember = Arrays.asList(hasMemberResultsNonMember.getResults());
        assertEquals(hasMemberResultsNonMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getWsSubject().getId(), TEST_USERNAMES.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultMetadata().getResultCode(), "IS_NOT_MEMBER");

        // Using uh number with a person object (one that is a member)
        hasMemberResultsIsMember = grouperApiService.hasMemberResults(
                GROUPING_INCLUDE, new Person(null, TEST_UH_NUMBERS.get(0), null));
        assertNotNull(hasMemberResultsIsMember);
        memberResultsIsMember = Arrays.asList(hasMemberResultsIsMember.getResults());
        assertEquals(hasMemberResultsIsMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getWsSubject().getId(), TEST_UH_NUMBERS.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultMetadata().getResultCode(), "IS_MEMBER");
        // Using uh number with a person object (one that is not a member)
        hasMemberResultsNonMember = grouperApiService.hasMemberResults(
                GROUPING_INCLUDE, new Person(null, TEST_UH_NUMBERS.get(1), null));
        assertNotNull(hasMemberResultsNonMember);
        memberResultsNonMember = Arrays.asList(hasMemberResultsNonMember.getResults());
        assertEquals(hasMemberResultsNonMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getWsSubject().getId(), TEST_UH_NUMBERS.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultMetadata().getResultCode(), "IS_NOT_MEMBER");

        // Using uh username with a person object (one that is a member)
        hasMemberResultsIsMember = grouperApiService.hasMemberResults(
                GROUPING_INCLUDE, new Person(null, TEST_UH_NUMBERS.get(0), TEST_USERNAMES.get(0)));
        assertNotNull(hasMemberResultsIsMember);
        memberResultsIsMember = Arrays.asList(hasMemberResultsIsMember.getResults());
        assertEquals(hasMemberResultsIsMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsIsMember.size(), 1);
        assertEquals(memberResultsIsMember.get(0).getWsSubject().getId(), TEST_USERNAMES.get(0));
        assertEquals(memberResultsIsMember.get(0).getResultMetadata().getResultCode(), "IS_MEMBER");
        // Using uh username with a person object (one that is not a member)
        hasMemberResultsNonMember = grouperApiService.hasMemberResults(
                GROUPING_INCLUDE, new Person(null, TEST_UH_NUMBERS.get(0), TEST_USERNAMES.get(1)));
        assertNotNull(hasMemberResultsNonMember);
        memberResultsNonMember = Arrays.asList(hasMemberResultsNonMember.getResults());
        assertEquals(hasMemberResultsNonMember.getWsGroup().getName(), GROUPING_INCLUDE);
        assertEquals(memberResultsNonMember.size(), 1);
        assertEquals(memberResultsNonMember.get(0).getWsSubject().getId(), TEST_USERNAMES.get(1));
        assertEquals(memberResultsNonMember.get(0).getResultMetadata().getResultCode(), "IS_NOT_MEMBER");

        try {
            grouperApiService.hasMemberResults(GROUPING_INCLUDE, new Person(null, null, null)).getResults();
        } catch (IllegalArgumentException e) {
            assertEquals("The person is required to have either a username or a uuid", e.getMessage());
        }

        // cleanup
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
    }

    @Test
    public void assignAttributesResultsTest() {
        String dateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        WsAttributeAssignValue wsAttributeAssignValue = grouperApiService.assignAttributeValue(dateTime);
        WsAssignAttributesResults assignAttributesResults = grouperApiService.assignAttributesResults(
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                GROUPING,
                YYYYMMDDTHHMM,
                OPERATION_REPLACE_VALUES,
                wsAttributeAssignValue);
        assertNotNull(assignAttributesResults);

        // attributeDefNames
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(assignAttributesResults.getWsAttributeDefNames());
        attributeDefNames.forEach(Assertions::assertNotNull);
        assertEquals(attributeDefNames.size(), 1);

        List<WsAssignAttributeResult> assignAttributeResult =
                Arrays.asList(assignAttributesResults.getWsAttributeAssignResults());
        assignAttributeResult.forEach(Assertions::assertNotNull);
        assertEquals(assignAttributeResult.size(), 1);

        // attribute assignments
        List<WsAttributeAssign> attributeAssign =
                Arrays.asList(assignAttributeResult.get(0).getWsAttributeAssigns());
        attributeAssign.forEach(Assertions::assertNotNull);
        assertEquals(attributeAssign.size(), 1);

        // values of the attribute assignments
        List<WsAttributeAssignValue> attributeAssignValue =
                Arrays.asList(attributeAssign.get(0).getWsAttributeAssignValues());
        assertEquals(attributeAssignValue.size(), 1);

        assertEquals(attributeDefNames.get(0).getName(), YYYYMMDDTHHMM);
        assertEquals(attributeAssign.get(0).getAttributeAssignType(), ASSIGN_TYPE_GROUP);
        assertEquals(attributeAssign.get(0).getOwnerGroupName(), GROUPING);
        assertEquals(attributeAssign.get(0).getAttributeDefNameName(), YYYYMMDDTHHMM);
        assertEquals(attributeAssignValue.get(0).getValueSystem(), dateTime);
    }

    @Test
    public void grouperPrivilegesLiteResultTest() {
        WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLiteResult =
                grouperApiService.assignGrouperPrivilegesLiteResult(GROUPING, PrivilegeType.IN.value(),
                        grouperApiService.subjectLookup(ADMIN), true);
        assertNotNull(assignGrouperPrivilegesLiteResult);
        WsGroup groups = assignGrouperPrivilegesLiteResult.getWsGroup();
        assertNotNull(groups);

        assertEquals(groups.getName(), GROUPING);
        assertEquals(assignGrouperPrivilegesLiteResult.getPrivilegeName(), PrivilegeType.IN.value());
        assertEquals(assignGrouperPrivilegesLiteResult.getWsSubject().getIdentifierLookup(), ADMIN);
    }

    @Test
    public void membershipsResultsTest() {
        WsGetMembershipsResults membershipsResults =
                grouperApiService.membershipsResults(GROUPING, grouperApiService.subjectLookup(ADMIN));
        assertNotNull(membershipsResults);
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

        // Overloaded membersResults test, only takes three parameters
        membersResults = grouperApiService.membersResults(
                ASSIGN_TYPE_GROUP,
                grouperApiService.subjectLookup(ADMIN),
                Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE)
        );
        assertNotNull(membersResults);
        getMembersResult = Arrays.asList(membersResults.getResults());
        subjectAttributeNames = Arrays.asList(membersResults.getSubjectAttributeNames());

        assertEquals(getMembersResult.size(), 2);
        getMembersResult.forEach(
                results -> assertTrue(results.getWsGroup().getName().equals(GROUPING_INCLUDE) ||
                        results.getWsGroup().getName().equals(GROUPING_EXCLUDE)));
        assertEquals(subjectAttributeNames.get(0), ASSIGN_TYPE_GROUP);
    }

    @Test
    public void groupsResultsTest() {
        WsGetGroupsResults uhUsernameResults =
                grouperApiService.groupsResults(TEST_USERNAMES.get(0));
        WsGetGroupsResults uhNumberResults =
                grouperApiService.groupsResults(TEST_UH_NUMBERS.get(0));

        List<WsGetGroupsResult> getGroupsResultUsername = Arrays.asList(uhUsernameResults.getResults());
        assertEquals(getGroupsResultUsername.size(), 1);
        assertEquals(getGroupsResultUsername.get(0).getWsSubject().getId(), TEST_USERNAMES.get(0));

        List<WsGetGroupsResult> getGroupsResultId = Arrays.asList(uhNumberResults.getResults());
        assertEquals(getGroupsResultId.size(), 1);
        assertEquals(getGroupsResultId.get(0).getWsSubject().getId(), TEST_UH_NUMBERS.get(0));
    }

    @Test
    public void subjectsResultsTest() {
        WsGetSubjectsResults subjectsResults =
                grouperApiService.subjectsResults(grouperApiService.subjectLookup(ADMIN));
        assertNotNull(subjectsResults);
        List<WsSubject> subject = Arrays.asList(subjectsResults.getWsSubjects());
        assertEquals(subject.size(), 1);
        assertEquals(subject.get(0).getIdentifierLookup(), ADMIN);
    }

    @Test
    public void subjectLookupTest() {
        WsSubjectLookup subjectLookup = grouperApiService.subjectLookup(TEST_USERNAMES.get(0));
        assertEquals(TEST_USERNAMES.get(0), subjectLookup.getSubjectIdentifier());
        assertNull(subjectLookup.getSubjectId());
        assertNull(subjectLookup.getSubjectSourceId());

        subjectLookup = grouperApiService.subjectLookup(TEST_UH_NUMBERS.get(0));
        assertEquals(TEST_UH_NUMBERS.get(0), subjectLookup.getSubjectId());
        assertNull(subjectLookup.getSubjectIdentifier());
        assertNull(subjectLookup.getSubjectSourceId());
    }

    @Test
    public void assignAttributeValueTest() {
        String dateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        WsAttributeAssignValue attributeAssignValue = grouperApiService.assignAttributeValue(dateTime);
        assertNotNull(attributeAssignValue);
        assertEquals(attributeAssignValue.getValueSystem(), dateTime);
    }
}
