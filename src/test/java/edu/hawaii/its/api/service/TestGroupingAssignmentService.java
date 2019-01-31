package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.api.GcGetAttributeAssignments;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.*;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
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

    @Value("${groupings.api.test.grouping_store_empty}")
    private String GROUPING_STORE_EMPTY;
    @Value("${groupings.api.test.grouping_store_empty_include}")
    private String GROUPING_STORE_EMPTY_INCLUDE;
    @Value("${groupings.api.test.grouping_store_empty_exclude}")
    private String GROUPING_STORE_EMPTY_EXCLUDE;
    @Value("${groupings.api.test.grouping_store_empty_owners}")
    private String GROUPING_STORE_EMPTY_OWNERS;

    @Value("${groupings.api.test.grouping_true_empty}")
    private String GROUPING_TRUE_EMPTY;
    @Value("${groupings.api.test.grouping_true_empty_include}")
    private String GROUPING_TRUE_EMPTY_INCLUDE;
    @Value("${groupings.api.test.grouping_true_empty_exclude}")
    private String GROUPING_TRUE_EMPTY_EXCLUDE;
    @Value("${groupings.api.test.grouping_true_empty_owners}")
    private String GROUPING_TRUE_EMPTY_OWNERS;

    @Value("${groupings.api.test.grouping_timeout_test}")
    private String GROUPING_TIMEOUT;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.usernames}")
    private String[] usernames;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private HelperService helperService;

    @Autowired
    public Environment env; // Just for the settings check.

    @PostConstruct
    public void init() {
        Assert.hasLength(env.getProperty("grouperClient.webService.url"),
                "property 'grouperClient.webService.url' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.login"),
                "property 'grouperClient.webService.login' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.password"),
                "property 'grouperClient.webService.password' is required");
    }

    @Before
    public void setUp() {
        groupAttributeService.changeListservStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeOptInStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, usernames[0], true);

        //put in include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(usernames[0]);
        includeNames.add(usernames[1]);
        includeNames.add(usernames[2]);
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, includeNames);

        //remove from exclude
        membershipService.addGroupingMemberByUsername(usernames[0], GROUPING, usernames[4]);
        membershipService.addGroupingMemberByUsername(usernames[0], GROUPING, usernames[5]);

        //add to exclude
        membershipService.deleteGroupingMemberByUsername(usernames[0], GROUPING, usernames[3]);

        // assign ownership
        memberAttributeService.assignOwnership(GROUPING_STORE_EMPTY, ADMIN, usernames[0]);
        memberAttributeService.assignOwnership(GROUPING_TRUE_EMPTY, ADMIN, usernames[0]);
    }

    @Test
    public void adminListsTest() {
        //try with non-admin
        AdminListsHolder info = groupingAssignmentService.adminLists(usernames[0]);
        assertNotNull(info);
        assertEquals(info.getAllGroupings().size(), 0);
        assertEquals(info.getAdminGroup().getMembers().size(), 0);
        assertEquals(info.getAdminGroup().getUsernames().size(), 0);
        assertEquals(info.getAdminGroup().getNames().size(), 0);
        assertEquals(info.getAdminGroup().getUuids().size(), 0);

        //todo What about with admin???
        AdminListsHolder infoAdmin = groupingAssignmentService.adminLists(ADMIN);
        assertNotNull(infoAdmin);
    }

    @Test
    public void updateLastModifiedTest() {
        // Test is accurate to the minute, and if checks to see if the current
        // time gets added to the lastModified attribute of a group if the
        // minute happens to change in between getting the time and setting
        // the time, the test will fail.

        final String group = GROUPING_INCLUDE;

        GroupingsServiceResult gsr = membershipService.updateLastModified(group);
        String dateStr = gsr.getAction().split(" to time ")[1];

        WsGetAttributeAssignmentsResults assignments =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, group, YYYYMMDDTHHMM);
        String assignedValue = assignments.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        assertEquals(dateStr, assignedValue);
    }

    @Test
    public void getGroupingTest() {

        // usernames[4] does not own grouping, method should return empty grouping
        Grouping grouping = groupingAssignmentService.getGrouping(GROUPING, usernames[4]);
        assertEquals(grouping.getPath(), "");
        assertEquals(grouping.getName(), "");
        assertEquals(grouping.getOwners().getMembers().size(), 0);
        assertEquals(grouping.getInclude().getMembers().size(), 0);
        assertEquals(grouping.getExclude().getMembers().size(), 0);
        assertEquals(grouping.getBasis().getMembers().size(), 0);
        assertEquals(grouping.getComposite().getMembers().size(), 0);

        grouping = groupingAssignmentService.getGrouping(GROUPING, usernames[0]);

        assertEquals(grouping.getPath(), GROUPING);

        // Testing for garbage uuid basis bug fix
        // List<String> list = grouping.getBasis().getUuids();

        assertTrue(grouping.getBasis().getUsernames().contains(usernames[3]));
        assertTrue(grouping.getBasis().getUsernames().contains(usernames[4]));
        assertTrue(grouping.getBasis().getUsernames().contains(usernames[5]));

        assertTrue(grouping.getComposite().getUsernames().contains(usernames[0]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[1]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[2]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[4]));
        assertTrue(grouping.getComposite().getUsernames().contains(usernames[5]));

        assertTrue(grouping.getExclude().getUsernames().contains(usernames[3]));

        assertTrue(grouping.getInclude().getUsernames().contains(usernames[0]));
        assertTrue(grouping.getInclude().getUsernames().contains(usernames[1]));
        assertTrue(grouping.getInclude().getUsernames().contains(usernames[2]));

        assertTrue(grouping.getOwners().getUsernames().contains(usernames[0]));
    }

    @Test
    public void getGoupingDescTest(){
        Grouping tester = groupingAssignmentService.getGrouping("hawaii.edu:custom:test:kahlin:descTest", ADMIN);

        System.out.println(tester.getDescription());
    }

    @Test
    public void getBasisGroupWithTimeoutTest() throws Exception {
//        Grouping grouping = groupingAssignmentService.getGrouping("hawaii.edu:custom:test:julio:jtest102-l", ADMIN);

        Group basisGroup = new Group();
        try {
            //todo Move to properties file
            basisGroup = groupingAssignmentService.getGroupMembers(ADMIN, GROUPING_TIMEOUT, BASIS);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe){
            assertThat(ghe.getStatusCode(), equalTo(504));
        }

        Group standardBasisGroup = groupingAssignmentService.getGroupMembers(ADMIN, GROUPING, BASIS);
        assertThat(standardBasisGroup.getMembers().size(), not(0));

        Group standardIncludeGroup = groupingAssignmentService.getGroupMembers(ADMIN, GROUPING, INCLUDE);
        assertThat(standardIncludeGroup.getMembers().size(), not(0));

        //todo Split basis into its own function, then check for GroupingsHTTPException
        //todo Split API calls in REST controller for each group (maybe a generic getGroup call?)
        //todo Write filter using direct matching using getMember
//        assertEquals(grouping.getPath(), "");
//        assertEquals(grouping.getName(), "");
//        assertEquals(grouping.getOwners().getMembers().size(), 2);
//        assertEquals(grouping.getInclude().getMembers().size(), 0);
//        assertEquals(grouping.getExclude().getMembers().size(), 0);
//        assertEquals(grouping.getBasis().getMembers().size(), 0);
//        assertEquals(grouping.getComposite().getMembers().size(), 0);
    }

    @Test
    public void getPaginatedGroupingTest() {

        // Paging starts at 1 D:
        // Page 1 contains 3 stale subjects, should return 17
        Grouping paginatedGroupingPage1 = groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 1, 20);
        // Page 2 contains 1 stale subject, should return 19
        Grouping paginatedGroupingPage2 = groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 2, 20);

        // Check to see the pages come out the right sizes
        assertThat(paginatedGroupingPage1.getBasis().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getInclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getExclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getComposite().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage1.getOwners().getMembers().size(), lessThanOrEqualTo(20));

        assertThat(paginatedGroupingPage2.getBasis().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getInclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getExclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getComposite().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGroupingPage2.getOwners().getMembers().size(), lessThanOrEqualTo(20));

        // Both pages should not be the same (assuming no groups are empty)
        assertThat(paginatedGroupingPage1.getBasis(), not(paginatedGroupingPage2.getBasis()));
        assertThat(paginatedGroupingPage1.getInclude(), not(paginatedGroupingPage2.getInclude()));
        assertThat(paginatedGroupingPage1.getExclude(), not(paginatedGroupingPage2.getExclude()));
        assertThat(paginatedGroupingPage1.getComposite(), not(paginatedGroupingPage2.getComposite()));
        assertThat(paginatedGroupingPage1.getOwners(), not(paginatedGroupingPage2.getOwners()));

        // Test paging without proper permissions
        Grouping paginatedGroupingPagePermissions = groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[1], 1, 20);
        assertThat(paginatedGroupingPagePermissions.getBasis().getMembers().size(), equalTo(0));
    }

    // todo Method not implemented
    @Ignore
    @Test
    public void getFilteredGroupingTest() {

        Group group = groupingAssignmentService.getPaginatedAndFilteredMembers(GROUPING, usernames[0], "zac", 1, 20);

    }

    // Testing why getting a grouping returns different results for a page of the size of the entire grouping
    // Results are the pagination automatically removes stale subjects for us, but doesn't get the full page
    // Plan is to leave as is, and some pages will be shorter than others
    // Maybe UI can show messages of some sort to say this is the case
    @Ignore
    @Test
    public void paginatedVersusNonpaginatedTest () {
        Grouping groupingNonPaginated = groupingAssignmentService.getGrouping(GROUPING, usernames[0]);
        Grouping groupingPaginated = groupingAssignmentService.getPaginatedGrouping(GROUPING, usernames[0], 1, 369);

        List<Person> paginatedBasisMembers = groupingPaginated.getBasis().getMembers();
        List<Person> nonPaginatedBasisMembers = groupingNonPaginated.getBasis().getMembers();

        List<String> uuids = new ArrayList<>();

        for(Person p : nonPaginatedBasisMembers) {
            uuids.add(p.getUuid());
        }

        Collections.sort(uuids);
    }

    @Test
    public void groupingsInTest() {
        GroupingAssignment groupingAssignment = groupingAssignmentService.getGroupingAssignment(usernames[0]);
        boolean isInGrouping = false;

        for (Grouping grouping : groupingAssignment.getGroupingsIn()) {
            if (grouping.getPath().contains(GROUPING)) {
                isInGrouping = true;
                break;
            }
        }
        assertTrue(isInGrouping);

        isInGrouping = false;
        groupingAssignment = groupingAssignmentService.getGroupingAssignment(usernames[3]);
        for (Grouping grouping : groupingAssignment.getGroupingsIn()) {
            if (grouping.getPath().contains(GROUPING)) {
                isInGrouping = true;
                break;
            }
        }
        assertFalse(isInGrouping);
    }

    @Test
    public void groupingsOwnedTest() {
        GroupingAssignment groupingAssignment = groupingAssignmentService.getGroupingAssignment(usernames[0]);
        boolean isGroupingOwner  = false;

        for (Grouping grouping : groupingAssignment.getGroupingsOwned()) {
            if (grouping.getPath().contains(GROUPING)) {
                isGroupingOwner = true;
                break;
            }
        }
        assertTrue(isGroupingOwner);

        isGroupingOwner = false;
        groupingAssignment = groupingAssignmentService.getGroupingAssignment(usernames[4]);
        for (Grouping grouping : groupingAssignment.getGroupingsOwned()) {
            if (grouping.getPath().contains(GROUPING)) {
                isGroupingOwner = true;
                break;
            }
        }
        assertFalse(isGroupingOwner);
    }

    @Test
    public void groupingsOptedTest() {
        //todo
    }

    @Test
    public void groupingsToOptTest() {
        GroupingAssignment groupingAssignment = groupingAssignmentService.getGroupingAssignment(usernames[0]);

        boolean isOptInPossible = false;
        for (Grouping grouping : groupingAssignment.getGroupingsToOptInTo()) {
            if (grouping.getPath().contains(GROUPING)) {
                isOptInPossible = true;
                break;
            }
        }
        assertFalse(isOptInPossible);

        boolean isOptOutPossible = false;
        for (Grouping grouping : groupingAssignment.getGroupingsToOptOutOf()) {
            if (grouping.getPath().contains(GROUPING)) {
                isOptOutPossible = true;
                break;
            }
        }
        assertTrue(isOptOutPossible);
    }

    @Test
    public void getMembersTest() {

        // Testing for garbage uuid basis bug fix
        // Group testGroup = groupingAssignmentService.getMembers(usernames[0], GROUPING_BASIS);

        List<String> groupings = new ArrayList<>();
        groupings.add(GROUPING);
        Group group = groupingAssignmentService.getMembers(usernames[0], groupings).get(GROUPING);
        List<String> usernames = group.getUsernames();

        assertTrue(usernames.contains(this.usernames[0]));
        assertTrue(usernames.contains(this.usernames[1]));
        assertTrue(usernames.contains(this.usernames[2]));
        assertFalse(usernames.contains(this.usernames[3]));
        assertTrue(usernames.contains(this.usernames[4]));
        assertTrue(usernames.contains(this.usernames[5]));
    }

    @Test
    public void getMembershipAssignmentTest() {
        // usernames[1] should already be in GROUPING
        List<String> groupingsIn = groupingAssignmentService
                .getMembershipAssignment(usernames[0], usernames[0])
                .getGroupingsIn()
                .stream()
                .map(Grouping::getPath)
                .collect(Collectors.toList());

        List<String> groupingsToOptInto = groupingAssignmentService
                .getMembershipAssignment(usernames[0], usernames[0])
                .getGroupingsToOptInTo()
                .stream()
                .map(Grouping::getPath)
                .collect(Collectors.toList());

        assertTrue(groupingsIn.contains(GROUPING));
        assertFalse(groupingsToOptInto.contains(GROUPING));

        // take usernames[1] out of GROUPING
        membershipService.deleteGroupingMemberByUsername(usernames[0], GROUPING, usernames[0]);

        // GROUPING has OPT-IN turned on, so usernames[1] should be able to opt back into GROUPING
        groupingsIn = groupingAssignmentService
                .getMembershipAssignment(usernames[0], usernames[0])
                .getGroupingsIn()
                .stream()
                .map(Grouping::getPath)
                .collect(Collectors.toList());

        groupingsToOptInto = groupingAssignmentService
                .getMembershipAssignment(usernames[0], usernames[0])
                .getGroupingsToOptInTo()
                .stream()
                .map(Grouping::getPath)
                .collect(Collectors.toList());

        assertFalse(groupingsIn.contains(GROUPING));
        assertTrue(groupingsToOptInto.contains(GROUPING));
    }

    @Test
    public void getGroupNamesTest() {
        List<String> groupNames1 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[1]);
        List<String> groupNames3 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[3]);

        //usernames[1] should be in the composite and the include, not basis or exclude
        assertTrue(groupNames1.contains(GROUPING));
        assertTrue(groupNames1.contains(GROUPING_INCLUDE));
        assertFalse(groupNames1.contains(GROUPING_BASIS));
        assertFalse(groupNames1.contains(GROUPING_EXCLUDE));

        //usernames[3] should be in the basis and exclude, not the composite or include
        assertTrue(groupNames3.contains(GROUPING_BASIS));
        assertTrue(groupNames3.contains(GROUPING_EXCLUDE));
        assertFalse(groupNames3.contains(GROUPING));
        assertFalse(groupNames3.contains(GROUPING_INCLUDE));
    }

    @Test
    public void getGroupNames() {
        List<String> groups = groupingAssignmentService.getGroupPaths(ADMIN, usernames[0]);

        assertTrue(groups.contains(GROUPING_OWNERS));
        assertTrue(groups.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups2 = groupingAssignmentService.getGroupPaths(ADMIN, usernames[1]);

        assertFalse(groups2.contains(GROUPING_OWNERS));
        assertFalse(groups2.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertFalse(groups2.contains(GROUPING_TRUE_EMPTY_OWNERS));
    }

    @Test
    public void getGroupPathsPermissionsTest(){
        List<String> groups = groupingAssignmentService.getGroupPaths(ADMIN, usernames[0]);

        assertTrue(groups.contains(GROUPING_OWNERS));
        assertTrue(groups.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups2 = groupingAssignmentService.getGroupPaths(usernames[0], usernames[0]);

        assertTrue(groups2.contains(GROUPING_OWNERS));
        assertTrue(groups2.contains(GROUPING_STORE_EMPTY_OWNERS));
        assertTrue(groups2.contains(GROUPING_TRUE_EMPTY_OWNERS));

        List<String> groups3 = groupingAssignmentService.getGroupPaths(usernames[1], usernames[0]);
        assertThat(groups3.size(), equalTo(0));

//        try{
//            groupingAssignmentService.getGroupPaths(usernames[1], usernames[0]);
//            fail("Shouldn't be here");
//        } catch (GroupingsHTTPException ghe) {
//            assertThat(ghe.getStatusCode(), equalTo(403));
//        }
    }

    @Test
    public void grouperTest() {
        List<String> groupPaths = groupingAssignmentService.getGroupPaths(ADMIN, usernames[0]);

        List<String> groupings = new ArrayList<>();
        List<String> groupings2 = new ArrayList<>();

        if (groupPaths.size() > 0) {

            List<WsAttributeAssign> attributes = new ArrayList<>();

            for (String path : groupPaths) {
                WsGetAttributeAssignmentsResults trioGroups = new GcGetAttributeAssignments()
                        .addAttributeDefNameName(TRIO)
                        .assignAttributeAssignType(ASSIGN_TYPE_GROUP)
                        .addOwnerGroupName(path)
                        .execute();

                if (trioGroups.getWsAttributeAssigns() != null) {
                    Collections.addAll(attributes, trioGroups.getWsAttributeAssigns());
                }
            }

            if (attributes.size() > 0) {
                groupings.addAll(attributes.stream().map(WsAttributeAssign::getOwnerGroupName)
                        .collect(Collectors.toList()));
            }

            assertNotNull(groupings);

            //////////////////////////////////////////////////////////////////////////////////

            GcGetAttributeAssignments trioGroups2 = new GcGetAttributeAssignments()
                    .addAttributeDefNameName(TRIO)
                    .assignAttributeAssignType(ASSIGN_TYPE_GROUP);

            groupPaths.forEach(trioGroups2::addOwnerGroupName);

            WsGetAttributeAssignmentsResults attributeAssignmentsResults2 = trioGroups2.execute();

            assertNotNull(attributeAssignmentsResults2);

            WsAttributeAssign[] wsGroups2 = attributeAssignmentsResults2.getWsAttributeAssigns();

            if (wsGroups2 != null && wsGroups2.length > 0) {
                for (WsAttributeAssign grouping : wsGroups2) {
                    groupings2.add(grouping.getOwnerGroupName());
                }
            }
        }

        assertNotNull(groupings2);

    }

    @Test
    public void makeGroupingsTest() {
        List<String> groupingPaths = new ArrayList<>();
        groupingPaths.add(GROUPING);
        groupingPaths.add(GROUPING_STORE_EMPTY);
        groupingPaths.add(GROUPING_TRUE_EMPTY);

        List<Grouping> groupings = helperService.makeGroupings(groupingPaths);

        assertTrue(groupings.size() == 3);
    }
}
