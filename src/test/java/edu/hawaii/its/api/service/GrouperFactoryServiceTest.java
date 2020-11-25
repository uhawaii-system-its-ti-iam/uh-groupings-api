package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

public class GrouperFactoryServiceTest {

    @Autowired
    private GrouperConfiguration grouperConfiguration;

    @Autowired
    private final GrouperFactoryServiceImplLocal grouperFactoryServiceImplLocal = new GrouperFactoryServiceImplLocal();
   
    @Autowired
    private HelperService hs;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    private static final String PATH_ROOT = "path:to:grouping";
    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;

    private final List<Person> admins = new ArrayList<>();
    private final List<Person> users = new ArrayList<>();
    private final Group adminGroup = new Group();
    private final Group appGroup = new Group();
    private final List<WsSubjectLookup> lookups = new ArrayList<>();

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void getSyncDestinationsTest() {
        assertTrue(grouperFactoryServiceImplLocal.getSyncDestinations().size() > 0);
    }

    @Test
    public void addEmptyGroupTest() {
        WsGroupSaveResults results = grouperFactoryServiceImplLocal.addEmptyGroup("username", GROUPING_3_PATH);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsSubjectLookupTest() {
        WsSubjectLookup subjectLookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        assertEquals("username0", subjectLookup.getSubjectIdentifier());
        subjectLookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(1).getUsername());
        assertEquals("username1", subjectLookup.getSubjectIdentifier());
        subjectLookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(2).getUsername());
        assertEquals("username2", subjectLookup.getSubjectIdentifier());
    }

    @Ignore
    @Test
    //These tests are calls to grouper web service functions within grouper and do not work on local test environment.
    public void deleteGroupTest() {
        WsSubjectLookup subjectLookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        WsGroupLookup groupLookup = grouperFactoryServiceImplLocal.makeWsGroupLookup(GROUPING_3_PATH);

        WsGroupDeleteResults results = grouperFactoryServiceImplLocal.deleteGroup(subjectLookup, groupLookup);

        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void getDescriptionTest() {
        String description = grouperFactoryServiceImplLocal.getDescription(GROUPING_0_PATH);
        assertNotNull(description);

        grouperFactoryServiceImplLocal.updateGroupDescription(GROUPING_0_PATH, "This is a description");
        description = grouperFactoryServiceImplLocal.getDescription(GROUPING_0_PATH);
        assertEquals(description, "This is a description");
    }

    //todo This tests a non-implemented function that returns null. Should adjust once function is implemented.
    @Test
    public void makeWsAddMemberResultsGroupTest() {
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        String groupUID = "";

        WsAddMemberResults results =
                grouperFactoryServiceImplLocal.makeWsAddMemberResultsGroup("groupPath", lookup, groupUID);
        assertNull(results);
    }

    //todo: adjust once method is fully implemented.
    @Test
    public void makeWsFindGroupResultsTest() {
        WsFindGroupsResults results = grouperFactoryServiceImplLocal.makeWsFindGroupsResults("groupPath");
        assertNotNull(results);
    }

    @Test
    public void makeWsGroupLookupTest() {
        WsGroupLookup lookup = grouperFactoryServiceImplLocal.makeWsGroupLookup("groupName");
        assertEquals("groupName", lookup.getGroupName());
    }

    @Test
    public void makeWsStemLookupTest() {
        WsStemLookup result;
        result = grouperFactoryServiceImplLocal.makeWsStemLookup("pre");
        assertNotNull(result);
        result = grouperFactoryServiceImplLocal.makeWsStemLookup("pre", grouperConfiguration.getTestUhuuid());
        assertNotNull(result);
    }

    @Test
    public void makeWsAttributeAssignValueTest() {
        WsAttributeAssignValue result;
        result = grouperFactoryServiceImplLocal.makeWsAttributeAssignValue("10:30AM");
        assertNotNull(result);
    }

    @Test
    public void makeWsStemSaveResultsTest() {
        WsStemSaveResults results = grouperFactoryServiceImplLocal.makeWsStemSaveResults("username", "stemPath");
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Ignore
    @Test
    //These tests are calls to grouper web service functions within grouper and do not work on local test enviornment.
    public void deleteStemTest() {
        WsSubjectLookup adminLookup =
                grouperFactoryServiceImplLocal.makeWsSubjectLookup(grouperConfiguration.getTestAdminUser());
        WsStemLookup stem = grouperFactoryServiceImplLocal.makeWsStemLookup("testStem");

        WsStemDeleteResults result = grouperFactoryServiceImplLocal.deleteStem(adminLookup, stem);
        System.out.print(result);
    }

    @Test
    public void makeWsAddMemberResultsTest() {
        WsAddMemberResults results;
        List<String> members = new ArrayList<>();
        members.add(users.get(0).getUsername());
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());

        results = grouperFactoryServiceImplLocal
                .makeWsAddMemberResults(GROUPING_3_PATH + ":exclude", lookup, users.get(5).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAddMemberResults(GROUPING_3_PATH + ":include", lookup, users.get(2).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal.makeWsAddMemberResults(GROUPING_3_PATH, lookup, members);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

    }

    @Test
    public void makeWsAddMemberResultsNewMemberTest() {
        WsAddMemberResults results;

        results = grouperFactoryServiceImplLocal.makeWsAddMemberResults(GROUPING_3_PATH, users.get(0).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsDeleteMemberResultsTest() {
        WsDeleteMemberResults results;

        results = grouperFactoryServiceImplLocal.makeWsDeleteMemberResults(GROUPING_3_PATH, users.get(5).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsDeleteMemberResultsLookupTest() {
        WsDeleteMemberResults results;
        List<String> members = new ArrayList<>();
        members.add(users.get(5).getUsername());
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(5).getUsername());

        results = grouperFactoryServiceImplLocal.makeWsDeleteMemberResults(GROUPING_3_PATH, lookup, members);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    // Unsure as to what the function does but it is covered
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTest() {
        String assignType = "placeholder";
        String attributeDefNameName = "palceholder";
        grouperFactoryServiceImplLocal.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTwoAttrTest() {
        String assignType = "placeholder";
        String attributeDefNameName0 = "palceholder";
        String attributeDefNameName1In = grouperConfiguration.getOptIn();
        String attributeDefNameName1Out = grouperConfiguration.getOptOut();

        grouperFactoryServiceImplLocal
                .makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0, attributeDefNameName1In);
        grouperFactoryServiceImplLocal
                .makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0, attributeDefNameName1Out);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioOwnerGroupNameTest() {
        List<String> ownerGroupNames = new ArrayList<>();
        String attributeDefNameName0 = "palceholder";
        String attributeDefNameName1In = grouperConfiguration.getOptIn();
        String attributeDefNameName1Out = grouperConfiguration.getOptOut();

        grouperFactoryServiceImplLocal.makeWsGetAttributeAssignmentsResultsTrio("assignType", attributeDefNameName0,
                attributeDefNameName1In, ownerGroupNames);
        grouperFactoryServiceImplLocal.makeWsGetAttributeAssignmentsResultsTrio("assignType", attributeDefNameName0,
                attributeDefNameName1Out, ownerGroupNames);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioOwnerTwoAttrGroupNameTest() {
        List<String> ownerGroupNames = new ArrayList<>();
        grouperFactoryServiceImplLocal
                .makeWsGetAttributeAssignmentsResultsTrio("assignType", "attributeDefNamName", ownerGroupNames);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForMembershipTest() {
        String assignType = "assignType";
        String attributeDefNameName = "work";
        String membershipId = users.get(5).getUsername();
        WsGetAttributeAssignmentsResults results = grouperFactoryServiceImplLocal
                .makeWsGetAttributeAssignmentsResultsForMembership(assignType, attributeDefNameName, membershipId);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupTest() {
        String assignType = "assignType";
        grouperFactoryServiceImplLocal.makeWsGetAttributeAssignmentsResultsForGroup(assignType, GROUPING_3_PATH);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupDefNameTest() {
        String assignType = "assignType";
        String attributeDefNameName = "attributeDefNameName";
        grouperFactoryServiceImplLocal
                .makeWsGetAttributeAssignmentsResultsForGroup(assignType, attributeDefNameName, GROUPING_3_PATH);
    }

    @Test
    public void makeWsHasMemberResultsTest() {
        grouperFactoryServiceImplLocal.makeWsHasMemberResults(GROUPING_3_PATH, "username101");
    }

    @Test
    public void makeWsHasMemberResultsPersonTest() {
        grouperFactoryServiceImplLocal.makeWsHasMemberResults(GROUPING_3_PATH, "username101");
    }

    @Test
    public void makeWsAssignAttributesResultsTest() {
        WsAssignAttributesResults results;

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResults("type", grouperConfiguration.getOperationRemoveAttribute(),
                        GROUPING_3_PATH, grouperConfiguration.getListserv(), "",
                        null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResults("type", grouperConfiguration.getOperationAssignAttribute(),
                        GROUPING_3_PATH, grouperConfiguration.getOptIn(), "",
                        null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResults("type", grouperConfiguration.getOperationAssignAttribute(),
                        GROUPING_3_PATH, grouperConfiguration.getOptOut(), "",
                        null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

    }

    @Test
    public void makeWsAssignAttributesResultsForMembershipTest() {
        WsAssignAttributesResults results;
        WsGetMembershipsResults getResults = hs.membershipsResults(users.get(0).getUsername(), GROUPING_3_PATH);
        String ownerID = hs.extractFirstMembershipID(getResults);

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForMembership("type", grouperConfiguration.getOperationAssignAttribute(),
                        "name", ownerID);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForMembership("type", grouperConfiguration.getOperationRemoveAttribute(),
                        "name", ownerID);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    // This test also takes care of setGroupingAttribute(grouping, attributeName, on)
    @Test
    public void makeWsAssignAttributesResultsForGroupTest() {
        WsAssignAttributesResults results;

        String assignType = "type";
        String assignOperation = grouperConfiguration.getOperationAssignAttribute();
        String removeOperation = grouperConfiguration.getOperationRemoveAttribute();
        String defName = grouperConfiguration.getListserv();
        String defName2 = grouperConfiguration.getOptIn();
        String defName3 = grouperConfiguration.getOptOut();
        String groupName = GROUPING_3_PATH;

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName2, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName3, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(assignType, assignOperation, "nothing", groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("FAILURE"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(assignType, removeOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsAssignAttributesResultsForGroupLookupVersionTest() {
        WsAssignAttributesResults results;
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        WsSubjectLookup lookup2 = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(3).getUsername());
        String assignType = "type";
        String assignOperation = grouperConfiguration.getOperationAssignAttribute();
        String defName = grouperConfiguration.getListserv();
        String groupName = GROUPING_3_PATH;

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(lookup, assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = grouperFactoryServiceImplLocal
                .makeWsAssignAttributesResultsForGroup(lookup2, assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("FAILURE"));
    }

    @Test
    public void makeWsAssignGrouperPrivilegesLiteResultTest() {
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        String privilegeNameIn = grouperConfiguration.getPrivilegeOptIn();
        String privilegeNameOut = grouperConfiguration.getPrivilegeOptOut();
        String groupName = GROUPING_3_PATH;

        grouperFactoryServiceImplLocal
                .makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeNameIn, lookup, true);
        grouperFactoryServiceImplLocal
                .makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeNameOut, lookup, true);
        try {
            grouperFactoryServiceImplLocal.makeWsAssignGrouperPrivilegesLiteResult(groupName, "illegal", lookup, true);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    @Test
    public void makeWsGetGrouperPrivilegesLiteResultExceptionTest() {
        WsSubjectLookup lookup = grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername());
        try {
            grouperFactoryServiceImplLocal.makeWsGetGrouperPrivilegesLiteResult(GROUPING_3_PATH, "illegal", lookup);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    @Test
    public void makeWsGetMembershipsResultsTest() {
        WsGetMembershipsResults result = grouperFactoryServiceImplLocal.makeWsGetMembershipsResults(GROUPING_0_PATH,
                grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername()));
        assertNotNull(result);
    }

    @Test
    public void makeWsGetAllMembershipsResultsTest() {
        List<String> groupNames = new ArrayList<>();
        groupNames.add(GROUPING_0_PATH);
        groupNames.add(GROUPING_1_PATH);
        groupNames.add(GROUPING_2_PATH);

        List<WsSubjectLookup> lookups = new ArrayList<>();
        lookups.add(grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(0).getUsername()));
        lookups.add(grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(1).getUsername()));
        lookups.add(grouperFactoryServiceImplLocal.makeWsSubjectLookup(users.get(2).getUsername()));

        List<WsGetMembershipsResults> result =
                grouperFactoryServiceImplLocal.makeWsGetAllMembershipsResults(groupNames, lookups);

        assertNotNull(result);
    }

    @Test
    public void toStringTest() {
        String str = grouperFactoryServiceImplLocal.toString();
        assertEquals(str, "GrouperFactoryServiceImplLocal [grouperConfiguration.getSettings()=" + grouperConfiguration
                .getSettings() + "]");
    }

    @Test
    public void makeEmptyWsAttributeAssignArrayTest() {
        WsAttributeAssign[] testArray;
        testArray = grouperFactoryServiceImplLocal.makeEmptyWsAttributeAssignArray();
        assertEquals(0, testArray.length);
    }

}