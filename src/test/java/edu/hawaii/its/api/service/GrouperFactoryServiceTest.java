package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

public class GrouperFactoryServiceTest {

    @Value("${groupings.api.settings}")
    private String SETTINGS;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    @Value("${groupings.api.privilege_opt_out}")
    private String PRIVILEGE_OPT_OUT;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.test.username}")
    private String USERNAME;

    @Value("${groupings.api.test.name}")
    private String NAME;

    @Value("${groupings.api.test.uhuuid}")
    private String UUID;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    private static final String PATH_ROOT = "path:to:grouping";

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;
    private static final String GROUPING_4_PATH = PATH_ROOT + 4;

    private static final String GROUPING_0_INCLUDE_PATH = GROUPING_0_PATH + ":include";
    private static final String GROUPING_0_OWNERS_PATH = GROUPING_0_PATH + ":owners";

    private static final String GROUPING_1_INCLUDE_PATH = GROUPING_1_PATH + ":include";
    private static final String GROUPING_1_EXCLUDE_PATH = GROUPING_1_PATH + ":exclude";

    private static final String GROUPING_2_INCLUDE_PATH = GROUPING_2_PATH + ":include";
    private static final String GROUPING_2_EXCLUDE_PATH = GROUPING_2_PATH + ":exclude";
    private static final String GROUPING_2_BASIS_PATH = GROUPING_2_PATH + ":basis";
    private static final String GROUPING_2_OWNERS_PATH = GROUPING_2_PATH + ":owners";

    private static final String GROUPING_3_INCLUDE_PATH = GROUPING_3_PATH + ":include";
    private static final String GROUPING_3_EXCLUDE_PATH = GROUPING_3_PATH + ":exclude";
    private static final String GROUPING_3_BASIS_PATH = GROUPING_3_PATH + ":basis";

    private static final String GROUPING_4_EXCLUDE_PATH = GROUPING_4_PATH + ":exclude";

    private static final String ADMIN_USER = "admin";
    private static final Person ADMIN_PERSON = new Person(ADMIN_USER, ADMIN_USER, ADMIN_USER);
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private static final String APP_USER = "app";
    private static final Person APP_PERSON = new Person(APP_USER, APP_USER, APP_USER);
    private List<Person> apps = new ArrayList<>();
    private Group appGroup = new Group();

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private GrouperFactoryService grouperFS;

    @Autowired
    private GrouperFactoryServiceImplLocal gfsl = new GrouperFactoryServiceImplLocal();

    @Autowired
    private HelperService hs;

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void addEmptyGroupTest() {
        WsGroupSaveResults results = gfsl.addEmptyGroup("username", GROUPING_3_PATH);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsSubjectLookupTest(){
        WsSubjectLookup subjectLookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());
        assertTrue(subjectLookup.getSubjectIdentifier().equals("username0"));
        subjectLookup = gfsl.makeWsSubjectLookup(users.get(1).getUsername());
        assertTrue(subjectLookup.getSubjectIdentifier().equals("username1"));
        subjectLookup = gfsl.makeWsSubjectLookup(users.get(2).getUsername());
        assertTrue(subjectLookup.getSubjectIdentifier().equals("username2"));
    }

    //todo currently not working, host parameter is null error
    @Test
    public void deleteGroupTest() {
        WsSubjectLookup subjectLookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());
        WsGroupLookup groupLookup = gfsl.makeWsGroupLookup(GROUPING_3_PATH);

        WsGroupDeleteResults results = gfsl.deleteGroup(subjectLookup, groupLookup);

        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void getDescriptionTest() {
        String description = gfsl.getDescription(GROUPING_0_PATH);
        assertNotNull(description);

        gfsl.updateGroupDescription(GROUPING_0_PATH, "This is a description");
        description = gfsl.getDescription(GROUPING_0_PATH);
        assertEquals(description, "This is a description");
    }

    //todo This tests a non-implemented function that returns null. Should adjust once function is implemented.
    @Test
    public void makeWsAddMemberResultsGroupTest() {
        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());
        String groupUID = "";

        WsAddMemberResults results = gfsl.makeWsAddMemberResultsGroup("groupPath", lookup, groupUID);
        assertTrue(results == null);
    }

    //todo: adjust once method is fully implemented.
    @Test
    public void makeWsFindGroupResultsTest() {
        WsFindGroupsResults results = gfsl.makeWsFindGroupsResults("groupPath");
        assertTrue(results != null);
    }

    @Test
    public void makeWsGroupLookupTest() {
        WsGroupLookup lookup = gfsl.makeWsGroupLookup("groupName");
        assertTrue(lookup.getGroupName().equals("groupName"));
    }

    @Test
    public void makeWsStemLookupTest() {
        WsStemLookup result;
        result = gfsl.makeWsStemLookup("pre");
        assertNotNull(result);
        result = gfsl.makeWsStemLookup("pre",UUID);
        assertNotNull(result);
    }

    @Test
    public void makeWsAttributeAssignValueTest() {
        WsAttributeAssignValue result;
        result = gfsl.makeWsAttributeAssignValue("10:30AM");
        assertNotNull(result);
    }

    @Test
    public void makeWsStemSaveResultsTest() {
        WsStemSaveResults results = gfsl.makeWsStemSaveResults("username", "stemPath");
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void deleteStemTest(){
        WsSubjectLookup adminLookup = gfsl.makeWsSubjectLookup(ADMIN);
        WsStemLookup stem = gfsl.makeWsStemLookup("testStem");

        WsStemDeleteResults result = gfsl.deleteStem(adminLookup, stem);
        System.out.print(result);
    }

    @Test
    public void makeWsAddMemberResultsTest() {
        WsAddMemberResults results;
        List<String> members = new ArrayList<>();
        members.add(users.get(0).getUsername());
        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());

        results = gfsl.makeWsAddMemberResults(GROUPING_3_EXCLUDE_PATH, lookup, users.get(5).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAddMemberResults(GROUPING_3_INCLUDE_PATH, lookup, users.get(2).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAddMemberResults(GROUPING_3_PATH, lookup, members);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

    }

    @Test
    public void makeWsAddMemberResultsNewMemberTest() {
        WsAddMemberResults results;

        results = gfsl.makeWsAddMemberResults(GROUPING_3_PATH, users.get(0).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsDeleteMemberResultsTest() {
        WsDeleteMemberResults results;

        results = gfsl.makeWsDeleteMemberResults(GROUPING_3_PATH, users.get(5).getUsername());
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsDeleteMemberResultsLookupTest() {
        WsDeleteMemberResults results;
        List<String> members = new ArrayList<>();
        members.add(users.get(5).getUsername());
        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(5).getUsername());

        results = gfsl.makeWsDeleteMemberResults(GROUPING_3_PATH, lookup, members);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    //Unsure as to what the function does but it is covered
    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTest() {
        WsGetAttributeAssignmentsResults results;

        String assignType = "placeholder";
        String attributeDefNameName = "palceholder";

        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName);

    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioTwoAttrTest() {
        WsGetAttributeAssignmentsResults results;

        String assignType = "placeholder";
        String attributeDefNameName0 = "palceholder";
        String attributeDefNameName1In = OPT_IN;
        String attributeDefNameName1Out = OPT_OUT;

        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0,
                attributeDefNameName1In);
        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio(assignType, attributeDefNameName0,
                attributeDefNameName1Out);

    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioOwnerGroupNameTest() {
        List<WsGetAttributeAssignmentsResults> results;

        List<String> ownerGroupNames = new ArrayList<>();
        String attributeDefNameName0 = "palceholder";
        String attributeDefNameName1In = OPT_IN;
        String attributeDefNameName1Out = OPT_OUT;

        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio("assignType", attributeDefNameName0,
                attributeDefNameName1In, ownerGroupNames);
        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio("assignType", attributeDefNameName0,
                attributeDefNameName1Out, ownerGroupNames);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsTrioOwnerTwoAttrGroupNameTest() {
        List<WsGetAttributeAssignmentsResults> results;

        List<String> ownerGroupNames = new ArrayList<>();

        results = gfsl.makeWsGetAttributeAssignmentsResultsTrio("assignType", "attributeDefNamName", ownerGroupNames);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForMembershipTest() {
        String assignType = "assignType";
        String attributeDefNameName = "work";
        String memebershipId = users.get(5).getUsername();

        users.get(5);

        WsGetAttributeAssignmentsResults results;

        results =
                gfsl.makeWsGetAttributeAssignmentsResultsForMembership(assignType, attributeDefNameName, memebershipId);

        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupTest() {
        WsGetAttributeAssignmentsResults results;

        String assignType = "assignType";

        results = gfsl.makeWsGetAttributeAssignmentsResultsForGroup(assignType, GROUPING_3_PATH);
    }

    @Test
    public void getSyncDestinationsTest() {

        assertTrue(gfsl.getSyncDestinations().size() > 0);
    }

    @Test
    public void makeWsGetAttributeAssignmentsResultsForGroupDefNameTest() {
        WsGetAttributeAssignmentsResults results;

        String assignType = "assignType";
        String attributeDefNameName = "attributeDefNameName";

        results = gfsl.makeWsGetAttributeAssignmentsResultsForGroup(assignType, attributeDefNameName, GROUPING_3_PATH);
    }

    @Test
    public void makeWsHasMemberResultsTest() {
        WsHasMemberResults results;

        results = gfsl.makeWsHasMemberResults(GROUPING_3_PATH, "username101");
    }

    @Test
    public void makeWsHasMemberResultsPersonTest() {
        WsHasMemberResults results;

        results = gfsl.makeWsHasMemberResults(GROUPING_3_PATH, "username101");
    }

    @Test
    public void makeWsAssignAttributesResultsTest() {
        WsAssignAttributesResults results;

        results = gfsl.makeWsAssignAttributesResults("type", OPERATION_REMOVE_ATTRIBUTE, GROUPING_3_PATH, LISTSERV, "",
                null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResults("type", OPERATION_ASSIGN_ATTRIBUTE, GROUPING_3_PATH, OPT_IN, "",
                null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResults("type", OPERATION_ASSIGN_ATTRIBUTE, GROUPING_3_PATH, OPT_OUT, "",
                null);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

    }

    @Test
    public void makeWsAssignAttributesResultsForMembershipTest() {
        WsAssignAttributesResults results;
        WsGetMembershipsResults getResults = hs.membershipsResults(users.get(0).getUsername(), GROUPING_3_PATH);
        String ownerID = hs.extractFirstMembershipID(getResults);

        results = gfsl.makeWsAssignAttributesResultsForMembership("type", OPERATION_ASSIGN_ATTRIBUTE, "name", ownerID);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResultsForMembership("type", OPERATION_REMOVE_ATTRIBUTE, "name", ownerID);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    // This test also takes care of setGroupingAttribute(grouping, attributeName, on)
    @Test
    public void makeWsAssignAttributesResultsForGroupTest() {
        WsAssignAttributesResults results;

        String assignType = "type";
        String assignOperation = OPERATION_ASSIGN_ATTRIBUTE;
        String removeOperation = OPERATION_REMOVE_ATTRIBUTE;
        String defName = LISTSERV;
        String defName2 = OPT_IN;
        String defName3 = OPT_OUT;
        String groupName = GROUPING_3_PATH;

        results = gfsl.makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName2, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResultsForGroup(assignType, assignOperation, defName3, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResultsForGroup(assignType, assignOperation, "nothing", groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("FAILURE"));

        results = gfsl.makeWsAssignAttributesResultsForGroup(assignType, removeOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));
    }

    @Test
    public void makeWsAssignAttributesResultsForGroupLookupVersionTest() {
        WsAssignAttributesResults results;

        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());
        WsSubjectLookup lookup2 = gfsl.makeWsSubjectLookup(users.get(3).getUsername());

        String assignType = "type";
        String assignOperation = OPERATION_ASSIGN_ATTRIBUTE;
        String removeOperation = OPERATION_REMOVE_ATTRIBUTE;
        String defName = LISTSERV;
        String defName2 = OPT_IN;
        String defName3 = OPT_OUT;
        String groupName = GROUPING_3_PATH;

        results = gfsl.makeWsAssignAttributesResultsForGroup(lookup, assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("SUCCESS"));

        results = gfsl.makeWsAssignAttributesResultsForGroup(lookup2, assignType, assignOperation, defName, groupName);
        assertTrue(results.getResultMetadata().getResultCode().startsWith("FAILURE"));
    }

    @Test
    public void makeWsAssignGrouperPrivilegesLiteResultTest() {
        WsAssignGrouperPrivilegesLiteResult result;

        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());

        String privilegeNameIn = PRIVILEGE_OPT_IN;
        String privilegeNameOut = PRIVILEGE_OPT_OUT;
        String groupName = GROUPING_3_PATH;
        Boolean isAllowed = true;

        result = gfsl.makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeNameIn, lookup, isAllowed);
        result = gfsl.makeWsAssignGrouperPrivilegesLiteResult(groupName, privilegeNameOut, lookup, isAllowed);

        try {
            result = gfsl.makeWsAssignGrouperPrivilegesLiteResult(groupName, "illegal", lookup, isAllowed);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }

    }

    @Test
    public void makeWsGetGrouperPrivilegesLiteResultExceptionTest() {
        WsGetGrouperPrivilegesLiteResult result;
        WsSubjectLookup lookup = gfsl.makeWsSubjectLookup(users.get(0).getUsername());

        try {
            result = gfsl.makeWsGetGrouperPrivilegesLiteResult(GROUPING_3_PATH, "illegal", lookup);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }

    }
    @Test
    public void makeWsGetMembershipsResultsTest() {
        WsGetMembershipsResults result = grouperFS.makeWsGetMembershipsResults(GROUPING_0_PATH, gfsl.makeWsSubjectLookup(users.get(0).getUsername()));
        assertTrue(result != null);
    }

    @Test
    public void makeWsGetAllMembershipsResultsTest() {
        List<String> groupNames = new ArrayList<>();
        groupNames.add(GROUPING_0_PATH);
        groupNames.add(GROUPING_1_PATH);
        groupNames.add(GROUPING_2_PATH);

        List<WsSubjectLookup> lookups = new ArrayList<>();
        lookups.add(gfsl.makeWsSubjectLookup(users.get(0).getUsername()));
        lookups.add(gfsl.makeWsSubjectLookup(users.get(1).getUsername()));
        lookups.add(gfsl.makeWsSubjectLookup(users.get(2).getUsername()));

        List<WsGetMembershipsResults> result = grouperFS.makeWsGetAllMembershipsResults(groupNames, lookups);

        assertTrue(result != null);
    }

    @Test
    public void toStringTest() {
        String str = gfsl.toString();
        assertTrue(str.equals("GrouperFactoryServiceImplLocal [SETTINGS=" + SETTINGS + "]"));
        //GrouperFactoryServiceImplLocal [SETTINGS=" + SETTINGS + "]
    }



    @Test
    public void addCompositeGroupTest() {
        //Not an actual test, addCompositeGroup is not implemented in GroupingFactoryServiceImpl.java
        //todo Build when main method is complete
        try {
            assertNull(
                    gfsl.addCompositeGroup(users.get(0).getUsername(), GROUPING_3_PATH, "type", GROUPING_3_BASIS_PATH,
                            GROUPING_3_INCLUDE_PATH));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }

    @Test
    public void makeEmptyWsAttributeAssignArrayTest() {
        WsAttributeAssign[] testArray;
        testArray = gfsl.makeEmptyWsAttributeAssignArray();
        assertTrue(testArray.length == 0);
    }

}