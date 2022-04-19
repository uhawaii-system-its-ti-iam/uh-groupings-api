package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
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

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.stem}")
    private String STEM;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Autowired
    GrouperApiService grouperApiService;

    @Autowired
    MemberAttributeService memberAttributeService;

    @Autowired
    MembershipService membershipService;

    @Autowired
    HelperService helperService;

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
        TEST_UH_NUMBERS.forEach(testUhNumber -> {
            grouperApiService.removeMember(GROUPING_ADMINS, testUhNumber);
            grouperApiService.removeMember(GROUPING_INCLUDE, testUhNumber);
            grouperApiService.removeMember(GROUPING_EXCLUDE, testUhNumber);
            grouperApiService.removeMember(GROUPING_OWNERS, testUhNumber);

            assertFalse(memberAttributeService.isOwner(GROUPING, testUhNumber));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUhNumber));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUhNumber));
            assertFalse(memberAttributeService.isAdmin(testUhNumber));
        });
    }

    @Test
    public void syncDestinationsTest() {
        TEST_UH_NUMBERS.forEach(
                num -> System.err.println(memberAttributeService.getMemberAttributes(ADMIN, num).toString()));
        assertNotNull(grouperApiService.syncDestinations());
    }

    @Test
    public void getDescriptionTest() {
        assertNotNull(grouperApiService.descriptionOf(GROUPING));
    }

    @Test
    public void updateGroupDescriptionTest() {
        String currentDescription = grouperApiService.descriptionOf(GROUPING);
        String defaultDescription = "Default Description for Test";
        WsGroupSaveResults groupSaveResults = grouperApiService.updateGroupDescription(GROUPING, defaultDescription);
        assertNotNull(groupSaveResults);
        assertNotNull(groupSaveResults.getResults());
        assertNotNull(groupSaveResults.getResults()[0]);
        assertNotNull(groupSaveResults.getResults()[0].getWsGroup());
        assertNotNull(groupSaveResults.getResults()[0].getWsGroup().getDescription());
        String updatedDescriptionResult = groupSaveResults.getResults()[0].getWsGroup().getDescription();
        assertEquals(defaultDescription, updatedDescriptionResult);
        assertEquals(updatedDescriptionResult, grouperApiService.descriptionOf(GROUPING));
        groupSaveResults = grouperApiService.updateGroupDescription(GROUPING, currentDescription);
        updatedDescriptionResult = groupSaveResults.getResults()[0].getWsGroup().getDescription();
        assertEquals(currentDescription, updatedDescriptionResult);
        assertEquals(updatedDescriptionResult, grouperApiService.descriptionOf(GROUPING));
    }

    @Test
    public void addMemberTest() {
        // With uh usernames.
        WsAddMemberResults addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(addMemberResults);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0)));

        addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, grouperApiService.subjectLookup(ADMIN),
                TEST_USERNAMES.get(1));
        assertNotNull(addMemberResults);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1)));
        //// Clean up
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));

        // With uh numbers.
        addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(addMemberResults);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0)));

        addMemberResults = grouperApiService.addMember(GROUPING_INCLUDE, grouperApiService.subjectLookup(ADMIN),
                TEST_UH_NUMBERS.get(1));
        assertNotNull(addMemberResults);
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1)));
        //// Clean up
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
    }

    @Test
    public void removeMemberTest() {
        // With uh usernames.
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1));
        WsDeleteMemberResults deleteMemberResults =
                grouperApiService.removeMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0));
        assertNotNull(deleteMemberResults);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(0)));
        deleteMemberResults = grouperApiService.removeMember(GROUPING_INCLUDE, grouperApiService.subjectLookup(ADMIN),
                TEST_USERNAMES.get(1));
        assertNotNull(deleteMemberResults);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_USERNAMES.get(1)));

        // With uh numbers.
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        grouperApiService.addMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1));
        deleteMemberResults = grouperApiService.removeMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(deleteMemberResults);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0)));
        deleteMemberResults = grouperApiService.removeMember(GROUPING_INCLUDE, grouperApiService.subjectLookup(ADMIN),
                TEST_UH_NUMBERS.get(1));
        assertNotNull(deleteMemberResults);
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(1)));
    }

    @Test
    public void groupsOfTest() {
        List<WsGroup> groups = Arrays.asList(grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, TRIO).getWsGroups());
        assertNotNull(groups);
        groups.forEach(Assertions::assertNotNull);
        groups.forEach(group -> assertEquals("group", group.getTypeOfGroup()));
    }

    @Test
    public void attributeAssignsTest() {
        List<WsAttributeAssign> attributeAssigns = Arrays.asList(
                grouperApiService.attributeAssigns(ASSIGN_TYPE_GROUP, TRIO, OPT_IN).getWsAttributeAssigns());
        assertNotNull(attributeAssigns);
        attributeAssigns.forEach(Assertions::assertNotNull);
        attributeAssigns.forEach(attributeAssign -> assertEquals("group", attributeAssign.getAttributeAssignType()));
    }

    @Test
    public void groupAttributeDefNames() {
        List<WsAttributeDefName> attributeDefNames =
                Arrays.asList(
                        grouperApiService.groupAttributeDefNames(ASSIGN_TYPE_GROUP, GROUPING).getWsAttributeDefNames());
        assertNotNull(attributeDefNames);
        attributeDefNames.forEach(Assertions::assertNotNull);
    }

    @Test
    public void groupAttributeAssignsTest() {
        WsAttributeAssign[] attributeAssigns =
                grouperApiService.groupAttributeAssigns(ASSIGN_TYPE_GROUP, OPT_IN, GROUPING).getWsAttributeAssigns();
        assertNotNull(attributeAssigns);
    }

    @Test
    public void hasMemberResultsTest() {
        WsHasMemberResults hasMemberResults =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(hasMemberResults);
        assertNotNull(hasMemberResults);

        hasMemberResults =
                grouperApiService.hasMemberResults(GROUPING_INCLUDE, new Person(null, TEST_USERNAMES.get(0), null));
        assertNotNull(hasMemberResults);

        hasMemberResults = grouperApiService.hasMemberResults(GROUPING_INCLUDE,
                new Person(null, TEST_USERNAMES.get(0), TEST_USERNAMES.get(0)));
        assertNotNull(hasMemberResults);

        try {
            grouperApiService.hasMemberResults(GROUPING_INCLUDE, new Person(null, null, null)).getResults();
        } catch (NullPointerException e) {
            assertEquals("The person is required to have either a username or a uuid", e.getMessage());
        }
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
    }

    @Test
    public void assignAttributesResultsForGroupTest() {
        WsAssignAttributesResults assignAttributesResults = grouperApiService.assignAttributesResultsForGroup(
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                OPT_IN,
                GROUPING);
        assertNotNull(assignAttributesResults);
    }

    @Test
    public void grouperPrivilegesLiteResultTest() {
        WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLiteResult =
                grouperApiService.assignGrouperPrivilegesLiteResult(GROUPING, PRIVILEGE_OPT_IN,
                        grouperApiService.subjectLookup(ADMIN), true);
        assertNotNull(assignGrouperPrivilegesLiteResult);
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

        //Overloaded membersResults test, only takes three parameters
        membersResults = grouperApiService.membersResults(
                ASSIGN_TYPE_GROUP,
                grouperApiService.subjectLookup(ADMIN),
                Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE)
        );
        assertNotNull(membersResults);
    }

    @Test
    public void groupsResultsTest() {
        WsGetGroupsResults uhUsernameResults =
                grouperApiService.groupsResults(TEST_USERNAMES.get(0), grouperApiService.stemLookup(STEM),
                        StemScope.ALL_IN_SUBTREE);
        WsGetGroupsResults uhNumberResults =
                grouperApiService.groupsResults(TEST_UH_NUMBERS.get(0), grouperApiService.stemLookup(STEM),
                        StemScope.ALL_IN_SUBTREE);
        assertNotNull(uhUsernameResults);
        assertNotNull(uhNumberResults);
        assertNotNull(uhUsernameResults.getResults());
        assertNotNull(uhNumberResults.getResults());
        assertNotNull(uhUsernameResults.getResults()[0]);
        assertNotNull(uhNumberResults.getResults()[0]);
        assertNotNull(uhUsernameResults.getResults()[0].getWsGroups());
        assertNotNull(uhNumberResults.getResults()[0].getWsGroups());
    }

    @Test
    public void subjectsResultsTest() {
        WsGetSubjectsResults subjectsResults =
                grouperApiService.subjectsResults(grouperApiService.subjectLookup(ADMIN));
        assertNotNull(subjectsResults);

    }

    @Test
    public void findGroupsResultsTest() {
        WsFindGroupsResults findGroupsResults = grouperApiService.findGroupsResults(GROUPING_INCLUDE);
        assertNotNull(findGroupsResults);
    }

    @Test
    public void subjectLookupTest() {
        TEST_USERNAMES.forEach(testUsername -> {
            WsSubjectLookup subjectLookup = grouperApiService.subjectLookup(testUsername);
            assertEquals(testUsername, subjectLookup.getSubjectIdentifier());
            assertNull(subjectLookup.getSubjectId());
            assertNull(subjectLookup.getSubjectSourceId());
        });
        TEST_UH_NUMBERS.forEach(testUhNumber -> {
            WsSubjectLookup subjectLookup = grouperApiService.subjectLookup(testUhNumber);
            assertEquals(testUhNumber, subjectLookup.getSubjectId());
            assertNull(subjectLookup.getSubjectIdentifier());
            assertNull(subjectLookup.getSubjectSourceId());
        });
    }

}
