package edu.hawaii.its.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingFactoryService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsRestControllerv2_1 {

    @Value("${groupings.api.test.admin_username}")
    private String ADMIN_USERNAME;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_indirect_basis}")
    private String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_store_empty}")
    private String GROUPING_STORE_EMPTY;

    @Value("${groupings.api.test.grouping_store_empty_include}")
    private String GROUPING_STORE_EMPTY_INCLUDE;

    @Value("${groupings.api.test.grouping_store_empty_exclude}")
    private String GROUPING_STORE_EMPTY_EXCLUDE;

    @Value("${groupings.api.test.grouping_true_empty}")
    private String GROUPING_TRUE_EMPTY;

    @Value("${groupings.api.test.grouping_true_empty_include}")
    private String GROUPING_TRUE_EMPTY_INCLUDE;

    @Value("${groupings.api.test.grouping_true_empty_exclude}")
    private String GROUPING_TRUE_EMPTY_EXCLUDE;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("${groupings.api.test.usernames}")
    private String[] tst;

    @Value("${groupings.api.test.names}")
    private String[] tstName;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${grouperClient.webService.login}")
    private String API_ACCOUNT;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.ldap}")
    private String LDAP;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GroupingFactoryService groupingFactoryService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private GroupingsRestControllerv2_1 gc;

    @Autowired
    public Environment env; // Just for the settings check.

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

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
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        //put in include
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[0]);
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[1]);
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[2]);

        //add to exclude
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_EXCLUDE, tst[3]);

        //remove from exclude
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[4]);
        membershipService.addGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[5]);

        groupAttributeService.changeOptOutStatus(GROUPING, tst[0], true);
        groupAttributeService.changeOptInStatus(GROUPING, tst[0], true);

        memberAttributeService.removeOwnership(GROUPING, tst[0], tst[1]);
    }

    @Test
    public void testConstruction() {
        assertNotNull(groupAttributeService);
        assertNotNull(groupingAssignmentService);
        assertNotNull(groupingFactoryService);
        assertNotNull(helperService);
        assertNotNull(memberAttributeService);
        assertNotNull(membershipService);
        assertNotNull(gc);
    }

    // iamtst01 does not have permissions, so this should fail
    @Test
    @WithMockUhUser(username = "iamtst01")
    public void adminsGroupingsFailTest() throws Exception {

        AdminListsHolder listHolderFail = mapAdminListsHolder();

        assertThat(listHolderFail.getAdminGroup().getMembers().size(), equalTo(0));
        assertThat(listHolderFail.getAllGroupings().size(), equalTo(0));

    }

    // app user has permissions to obtain this data
    @Test
    @WithMockUhUser(username = "_groupings_api_2")
    public void adminsGroupingsPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder();

        // ADMIN_USERNAME can be replaced with any account that has admin access
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(ADMIN_USERNAME));
        assertThat(listHolderPass.getAllGroupings().size(), not(0));

    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void memberAttributesTest() throws Exception {

        Map attributes = mapGetUserAttributes("iamtst01");

        assertThat(attributes.get("uid"), equalTo("iamtst01"));
        assertThat(attributes.get("givenName"), equalTo("tst01name"));
        assertThat(attributes.get("uhuuid"), equalTo("iamtst01"));
        assertThat(attributes.get("cn"), equalTo("tst01name"));
        assertThat(attributes.get("sn"), equalTo("tst01name"));

        // Test with username not in database
        try {
            mapGetUserAttributes("bobjones");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test with null field
        try {
            mapGetUserAttributes(null);
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void memberGroupingsTest() throws Exception {

        List listMemberships = mapList("/api/groupings/v2.1/members/iamtst01/groupings", "get");
        assertThat(listMemberships.size(), not(0));

        // Test with username not in database
        try {
            mapList("/api/groupings/v2.1/members/bobjones/groupings", "get");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test with null field
        try {
            mapList("/api/groupings/v2.1/members/" + null + "/groupings", "get");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void ownerGroupingsTest() throws Exception {

        //        List listGroupings = mapOwnerGroupings("iamtst01");
        List listGroupings = mapList("/api/groupings/v2.1/owners/iamtst01/groupings", "get");
        assertThat(listGroupings.size(), not(0));

        // Test with username not in database
        try {
            mapList("/api/groupings/v2.1/owners/bobjones/groupings", "get");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test with null field
        try {
            mapList("/api/groupings/v2.1/owners/" + null + "/groupings", "get");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void getGroupingTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING);
        Group basis = grouping.getBasis();
        Group composite = grouping.getComposite();
        Group exclude = grouping.getExclude();
        Group include = grouping.getInclude();

        //basis
        assertTrue(basis.getUsernames().contains(tst[3]));
        assertTrue(basis.getUsernames().contains(tst[4]));
        assertTrue(basis.getUsernames().contains(tst[5]));
        assertTrue(basis.getNames().contains(tstName[3]));
        assertTrue(basis.getNames().contains(tstName[4]));
        assertTrue(basis.getNames().contains(tstName[5]));

        //composite
        assertTrue(composite.getUsernames().contains(tst[0]));
        assertTrue(composite.getUsernames().contains(tst[1]));
        assertTrue(composite.getUsernames().contains(tst[2]));
        assertTrue(composite.getUsernames().contains(tst[4]));
        assertTrue(composite.getUsernames().contains(tst[5]));
        assertTrue(composite.getNames().contains(tstName[0]));
        assertTrue(composite.getNames().contains(tstName[1]));
        assertTrue(composite.getNames().contains(tstName[2]));
        assertTrue(composite.getNames().contains(tstName[4]));
        assertTrue(composite.getNames().contains(tstName[5]));

        //exclude
        assertTrue(exclude.getUsernames().contains(tst[3]));
        assertTrue(exclude.getNames().contains(tstName[3]));

        //include
        assertTrue(include.getUsernames().contains(tst[0]));
        assertTrue(include.getUsernames().contains(tst[1]));
        assertTrue(include.getUsernames().contains(tst[2]));
        assertTrue(include.getNames().contains(tstName[0]));
        assertTrue(include.getNames().contains(tstName[1]));
        assertTrue(include.getNames().contains(tstName[2]));

        assertFalse(grouping.getOwners().getNames().contains(tstName[5]));

        // Test with a grouping that does not exist in database
        try {
            mapGrouping("thisIsNotARealGrouping");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test with null field
        try {
            mapGrouping(null);
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "_groupings_api_2")
    public void addDeleteAdminPassTest() throws Exception {

        // Make sure "iamtst01" isn't an admin
        AdminListsHolder listHolderPass = mapAdminListsHolder();
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        // Add "iamtst01" to admin list and check if it worked
        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post");
        listHolderPass = mapAdminListsHolder();
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        // Delete "iamtst01" from admin list and check if it worked
        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "delete");
        listHolderPass = mapAdminListsHolder();
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        // Test addAdmin with a name not in database
        try {
            mapGSR("/api/groupings/v2.1/admins/bobjones", "post");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test addAdmin with null field
        try {
            mapGSR("/api/groupings/v2.1/admins/" + null, "post");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test deleteAdmin with a name not in database
        try {
            mapGSR("/api/groupings/v2.1/admins/bobjones", "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test deleteAdmin with null field
        try {
            mapGSR("/api/groupings/v2.1/admins/" + null, "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteAdminFailTest() throws Exception {

        // Nothing in this test should go through since "iamtst01" is not an admin
        // Try addAdmin without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/admins/" + tst[1], "post");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Try deleteAdmin without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/admins/kahlin", "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteOwnerPassTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING);
        assertFalse(grouping.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[1], "put");

        grouping = mapGrouping(GROUPING);
        assertTrue(grouping.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[1], "delete");

        grouping = mapGrouping(GROUPING);
        assertFalse(grouping.getOwners().getUsernames().contains(tst[1]));

        // Test with information not in database
        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bobjones", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bobjones", "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Test with null fields
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + null + "/owners/" + null, "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + null + "/owners/" + null, "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void addDeleteOwnerFailTest() throws Exception {

        // This shouldn't go through because "iamtst02" is not an owner
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[2], "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[0], "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteMemberPassTest() throws Exception {

        // Check that "iamtst04" is not in include, but is in exclude
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "put");

        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "delete");

        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));

        // Check that "iamtst03" is not in exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "put");

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "delete");

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));

        // Reset database
        // "iamtst04" back in exclude, "iamtst03" back in include
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[2], "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[3], "put");

        // Garbage data tests
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bobjones", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bobjones", "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bobjones", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bobjones", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Null fields tests
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/includeMembers/" + null, "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/includeMembers/" + null, "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/excludeMembers/" + null, "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/excludeMembers/" + null, "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void addDeleteMemberFailTest() throws Exception {

        // Nothing in this test should go through since "iamtst02" is not an owner
        // Try add member to include without proper permissions
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Try delete member from include without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[2], "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Try add member to exclude without proper permissions
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Try delete member from exclude without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[3], "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void enableDisablePreferencesPassTest() throws Exception {

        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasLdap(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LDAP + "/enable", "put");

        assertFalse(groupAttributeService.optInPermission(GROUPING));
        assertFalse(groupAttributeService.optOutPermission(GROUPING));
        assertFalse(groupAttributeService.hasListserv(GROUPING));
        assertTrue(groupAttributeService.hasLdap(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LDAP + "/disable", "put");

        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasLdap(GROUPING));

        // Try with bad data
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/disable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/disable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        // Try with null fields
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/preferences/" + null + "/enable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + null + "/preferences/" + null + "/disable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void enableDisablePreferencesFailTest() throws Exception {

        // This should fail because "iamtst02" is not an owner
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    @Test
    @WithMockUhUser(username = "_groupings_api_2")
    public void addDeleteGroupingPassTest() throws Exception {
        //Can choose any grouping path we want here
        String newGrouping = "hawaii.edu:custom:test:ksanidad:ks-test";

        // Check if grouping already exists (it shouldn't)
        try{
            mapGrouping(newGrouping);
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), not(200));
        }

        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "post");
        mapGrouping(newGrouping);

        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "delete");

        try{
            mapGrouping(newGrouping);
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), not(200));
        }

    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteGroupingFailTest() throws Exception {

        // This should fail, "iamtst01" doesn't have proper permissions
        try{
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:ks-test", "post");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }

        try{
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:ksanidad-test", "delete");
        } catch (GroupingsHTTPException ghe) {
            ghe.printStackTrace();
        }
    }

    //todo v2.2 tests (right now these endpoints just throw UnsupportedOperationException, pointless to test)

    ///////////////////////////////////////////////////////////////////////
    // MVC mapping
    //////////////////////////////////////////////////////////////////////
    //todo Refactor as new default methods
    //todo Can probably condense into fewer methods if necessary

    private Map mapGetUserAttributes(String username) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/members/" + username)
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private List mapList(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private AdminListsHolder mapAdminListsHolder() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/adminsGroupings")
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), AdminListsHolder.class);
    }

    private Grouping mapGrouping(String groupingPath) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/groupings/" + groupingPath)
                .with(csrf()))
                .andReturn();
        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Grouping.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private GroupingsServiceResult mapGSR(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), GroupingsServiceResult.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private List mapGSRs(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private MvcResult mapHelper(String uri, String httpCall) throws Exception {

        MvcResult result;

        switch (httpCall) {
            case "get":
                result = mockMvc.perform(get(uri)
                        .with(csrf()))
                        .andReturn();
                break;
            case "post":
                result = mockMvc.perform(post(uri)
                        .with(csrf()))
                        .andReturn();
                break;
            case "put":
                result = mockMvc.perform(put(uri)
                        .with(csrf()))
                        .andReturn();
                break;
            case "delete":
                result = mockMvc.perform(delete(uri)
                        .with(csrf()))
                        .andReturn();
                break;
            default:
                throw new IllegalArgumentException();
        }
        return result;
    }
}