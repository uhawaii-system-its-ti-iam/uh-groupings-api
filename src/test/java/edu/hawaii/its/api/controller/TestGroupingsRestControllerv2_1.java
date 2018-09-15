package edu.hawaii.its.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import jdk.jfr.events.ExceptionThrownEvent;
import org.hibernate.annotations.WhereJoinTable;
import com.sun.net.httpserver.Authenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithAnonymousUser;
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
import edu.hawaii.its.api.type.GroupingServiceResultExceptionTest;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResultException;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(TestGroupingsRestControllerv2_1.class);

    @Value("${grouperClient.webService.login}")
    private String APP_USER;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_awy}")
    private String AWY_GROUPING;

    @Value("${groupings.api.test.grouping_awy_include}")
    private String AWY_INCLUDE;

    @Value("${groupings.api.test.grouping_awy_exclude}")
    private String AWY_EXCLUDE;

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

    @Value("${groupings.api.success}")
    private String SUCCESS;

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

    @Value("${groupings.api.test.uhuuids}")
    private String[] tstUuid;

    @Value("${groupings.api.test.grouping_awy_owners}")
    private String OWNERS;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

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

        // Remove appropriate privileges
        membershipService.deleteAdmin(ADMIN, tst[0]);
        memberAttributeService.removeOwnership(GROUPING, tst[0], tst[1]);

        // Add "iamtst03" to include and remove from exclude
        membershipService.addGroupMember(tst[0], GROUPING_INCLUDE, tst[2]);
        membershipService.deleteGroupMemberByUsername(tst[0], GROUPING_EXCLUDE, tst[2]);

        //Remove "iamtst04" from include and add to exclude
        membershipService.deleteGroupMemberByUsername(tst[0], GROUPING_INCLUDE, tst[3]);
        membershipService.addGroupMember(tst[0], GROUPING_EXCLUDE, tst[3]);

        //Reset preferences
        groupAttributeService.changeOptInStatus(GROUPING, tst[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, tst[0], true);
        groupAttributeService.changeListservStatus(GROUPING, tst[0], true);
        groupAttributeService.changeReleasedGroupingStatus(GROUPING, tst[0], false);

        // Delete grouping if it exists
        try{
            groupingFactoryService.deleteGrouping(ADMIN, "hawaii.edu:custom:test:ksanidad:bw-test");
        } catch (GroupingsServiceResultException gsre) {
            logger.info("Grouping doesn't exist.");
        }

        // Initialize test uuids
        tstUuid[0] = "10976564";
        tstUuid[1] = "11077773";
        tstUuid[2] = "11077784";
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

    //todo Check status codes and all possible end cases. They may be off or different from what we expect.

    // iamtst01 does not have permissions, so this should fail
    @Test
    @WithMockUhUser(username = "iamtst01")
    public void adminsGroupingsFailTest() throws Exception {

        AdminListsHolder listHolderFail = mapAdminListsHolder();

        assertThat(listHolderFail.getAdminGroup().getMembers().size(), equalTo(0));
        assertThat(listHolderFail.getAllGroupings().size(), equalTo(0));
    }

    // app user has permissions to obtain this data
    //todo Fix to replace _groupings_api_2 with APP_USER
    //todo Create new WithMockUhAdmin? Maybe?
    //todo Check result>session>attributes>0>value>authentication of both MockUHUser w/ _groupings_api_2 and MockAdminUser
    //todo What are the differences? maybe this'll help me see where I went wrong
    @Test
    @WithMockAdminUser
//    @WithMockUhUser(username = "_groupings_api_2")
    public void adminsGroupingsPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder();

        // ADMIN can be replaced with any account username that has admin access
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(ADMIN));
        assertThat(listHolderPass.getAllGroupings().size(), not(0));

    }

    // Anonymous user (not logged in) should be redirected
    @Test
    @WithAnonymousUser
    public void adminsGroupingsAnonTest() throws Exception {

        try {
            mapAdminListsHolder();
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void memberAttributesTest() throws Exception {

        Map attributes = mapGetUserAttributes(tst[0]);

        assertThat(attributes.get("uid"), equalTo("iamtst01"));
        assertThat(attributes.get("givenName"), equalTo("tst01name"));
        assertThat(attributes.get("uhuuid"), equalTo("iamtst01"));
        assertThat(attributes.get("cn"), equalTo("tst01name"));
        assertThat(attributes.get("sn"), equalTo("tst01name"));

        // Test with username not in database
        try {
            mapGetUserAttributes("bobjones");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Test with blank field
        try {
            mapGetUserAttributes("");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithAnonymousUser
    public void memberAttributesAnonTest() throws Exception {

        try {
            mapGetUserAttributes(tst[0]);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void memberGroupingsTest() throws Exception {

        List listMemberships = mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get");
        assertThat(listMemberships.size(), not(0));

        // Test with username not in database
        try {
            mapList("/api/groupings/v2.1/members/bobjones/groupings", "get");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Test with empty field
        try {
            mapList("/api/groupings/v2.1/members//groupings", "get");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithAnonymousUser
    public void memberGroupingsAnonTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void ownerGroupingsTest() throws Exception {

        List listGroupings = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get");
        assertThat(listGroupings.size(), not(0));

        // Test with username not in database
        try {
            mapList("/api/groupings/v2.1/owners/bobjones/groupings", "get");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Test with empty field
        try {
            mapList("/api/groupings/v2.1/owners//groupings", "get");
            fail("Shouldn't be here");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithAnonymousUser
    public void ownerGroupingsAnonTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void getGroupingPassTest() throws Exception {

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
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Test with empty field
        try {
            mapGrouping("");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void getGroupingFailTest() throws Exception {

        // Nothing in this test should go through since "iamtst02" is not an owner/superuser
        // In this case, the call succeeds, but the result will be an empty grouping
        Grouping grouping = mapGrouping(GROUPING);
        assertThat(grouping.getBasis().getUsernames().size(), equalTo(0));
        assertThat(grouping.getInclude().getUsernames().size(), equalTo(0));
        assertThat(grouping.getExclude().getUsernames().size(), equalTo(0));
        assertThat(grouping.getComposite().getUsernames().size(), equalTo(0));
    }

    @Test
    @WithAnonymousUser
    public void getGroupingsAnonTest() throws Exception {

        try {
            mapGrouping(GROUPING);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    //todo Fix to replace _groupings_api_2 with APP_USER
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

        //Try to add "iamtst01" again. It should do nothing but return 200 OK anyway.
        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post");
        listHolderPass = mapAdminListsHolder();
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        // Delete "iamtst01" from admin list and check if it worked
        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "delete");
        listHolderPass = mapAdminListsHolder();
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        // Test addAdmin with a name not in database
        try {
            mapGSR("/api/groupings/v2.1/admins/bobjones/", "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            listHolderPass = mapAdminListsHolder();
            assertFalse(listHolderPass.getAdminGroup().getUsernames().contains("bobjones"));
        }

        // Test addAdmin with empty field
        try {
            mapGSR("/api/groupings/v2.1/admins//", "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            listHolderPass = mapAdminListsHolder();
            assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(""));
        }

        // Test deleteAdmin with a non-admin (or even username in db, for that matter; deleteAdmin makes no distinction)
        GroupingsServiceResult gsr = mapGSR("/api/groupings/v2.1/admins/bobjones/", "delete");
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteAdminFailTest() throws Exception {

        // Nothing in this test should go through since "iamtst01" is not an admin

        // Try addAdmin without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/admins/" + tst[1], "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        // Try deleteAdmin without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/admins/" + ADMIN, "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithAnonymousUser
    public void addDeleteAdminAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/admins" + ADMIN, "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
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

        // Looks for username, then grouping
        // Test with information not in database
        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bobjones", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Looks for grouping, then username in grouping
        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bobjones", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Test with one empty field
        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/" + tst[0], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            List list = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get");
            list.contains("someGrouping");
        }

        // Looks for grouping, then username in grouping
        // Returns success even though the user is not in grouping
        GroupingsServiceResult gsr = mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/bobjones", "delete");
        gsr.getResultCode().startsWith(SUCCESS);

        // Test with empty fields
        try {
            mapGSR("/api/groupings/v2.1/groupings//owners//", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings//owners//", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void addDeleteOwnerFailTest() throws Exception {

        // This shouldn't go through because "iamtst02" is not an owner
        // Can't pull up owner list without being an owner
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[2], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[0], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithAnonymousUser
    public void addDeleteOwnerAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/owners/" + tst[0], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/owners/" + tst[0], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst05")
    public void addMemberUuidPassTest() throws Exception {
        mapGSRs("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/includeMembers/" + tstUuid[0], "put");

        // tests tstUuid[0] is in include but not exclude
        assertTrue(memberAttributeService.isMember(AWY_INCLUDE, tstUuid[0]));
        assertFalse(memberAttributeService.isMember(AWY_EXCLUDE, tstUuid[0]));

        // tests tstUuid[1] is in exclude
        mapGSRs("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/excludeMembers/" + tstUuid[1], "put");
        assertFalse(memberAttributeService.isMember(AWY_INCLUDE, tstUuid[1]));
        assertTrue(memberAttributeService.isMember(AWY_EXCLUDE, tstUuid[1]));
    }

    @Test
    @WithMockUhUser(username = "iamtst05")
    public void deleteMemberUuidPassTest() throws Exception {
        // confirm tstUuid[0] deleted from include group
        mapGSR("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/includeMembers/" + tstUuid[0], "delete");
        assertFalse(memberAttributeService.isMember(AWY_INCLUDE, tstUuid[0]));

        // confirm tstUuid[1] is deleted from exclude group
        mapGSR("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/excludeMembers/" + tstUuid[1], "delete");
        assertFalse(memberAttributeService.isMember(AWY_EXCLUDE, tst[1]));
    }

    @Test
    @WithMockUhUser(username = "iamtst05")
    public void addDeleteOwnerUuidPassTest() throws Exception {
        // User added as owner to AWY_GROUPING
        mapGSR("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/owners/" + tstUuid[0], "put");
        assertTrue(memberAttributeService.isMember(OWNERS, tstUuid[0]));

        mapGSR("/api/groupings/v2.1/groupings/" + AWY_GROUPING + "/owners/" + tstUuid[0], "delete");
        assertFalse(memberAttributeService.isMember(OWNERS, tstUuid[0]));
    }

    @Test
    @WithMockUhUser(username = "iamtst05")
    public void addDeleteAdminUuidPassTest() throws Exception {
        mapGSR("/api/groupings/v2.1/admins/" + tstUuid[0], "post");
        assertTrue(memberAttributeService.isAdmin(tstUuid[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tstUuid[0], "delete");
        assertFalse(memberAttributeService.isAdmin(tstUuid[0]));
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
        //todo Test all permutations of bad data
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bobjones", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bobjones", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bobjones", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bobjones", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Empty fields tests
        try {
            mapGSRs("/api/groupings/v2.1/groupings//includeMembers//", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//includeMembers//", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//excludeMembers//", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//excludeMembers//", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void addDeleteMemberFailTest() throws Exception {

        // Nothing in this test should go through since "iamtst02" is not an owner
        // Try add member to include without proper permissions
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        // Try delete member from include without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[2], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        // Try add member to exclude without proper permissions
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        // Try delete member from exclude without proper permissions
        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[3], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithAnonymousUser
    public void addDeleteMemberAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/includeMembers/" + tst[2], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/includeMembers/" + tst[2], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/excludeMembers/" + tst[2], "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/excludeMembers/" + tst[2], "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void enableDisablePreferencesPassTest() throws Exception {

        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasReleasedGrouping(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/disable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + RELEASED_GROUPING + "/enable", "put");

        assertFalse(groupAttributeService.optInPermission(GROUPING));
        assertFalse(groupAttributeService.optOutPermission(GROUPING));
        assertFalse(groupAttributeService.hasListserv(GROUPING));
        assertTrue(groupAttributeService.hasReleasedGrouping(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/enable", "put");
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + RELEASED_GROUPING + "/disable", "put");

        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasReleasedGrouping(GROUPING));

        // Try with bad data
        //todo Test all permutations of bad data
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/enable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/disable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        // Try with empty fields
        //todo Should maybe throw 501? I guess it doesn't care if preference name is ""
        try {
            mapGSRs("/api/groupings/v2.1/groupings//preferences//enable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//preferences//disable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst02")
    public void enableDisablePreferencesFailTest() throws Exception {

        // This should fail because "iamtst02" is not an owner
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithAnonymousUser
    public void enableDisablePreferencesAnonTest() throws Exception {

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    //todo Fix to replace _groupings_api_2 with APP_USER
    @Test
    @WithMockUhUser(username = "_groupings_api_2")
    public void addDeleteGroupingPassTest() throws Exception {
        //Can choose any grouping path we want here
        String newGrouping = "hawaii.edu:custom:test:ksanidad:bw-test";

        // Check if grouping already exists (it shouldn't)
        try {
            mapGrouping(newGrouping);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Make the grouping
        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "post");

        // Grouping should exist now
        try {
            mapGrouping(newGrouping);
        } catch (GroupingsHTTPException ghe) {
            fail("Shouldn't be here.");
        }

        // Delete the grouping
        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "delete");

        // Check to see the grouping is gone
        //todo Might need to refactor depending on outcome of deleteGrouping
        try {
            mapGrouping(newGrouping);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void addDeleteGroupingFailTest() throws Exception {

        // This should fail, "iamtst01" doesn't have proper permissions
        try {
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:bw-test", "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:ksanidad-test", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    @WithAnonymousUser
    public void addDeleteGroupingAnonTest() throws Exception {

        // This should fail, "iamtst01" doesn't have proper permissions
        try {
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:bw-test", "post");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapList("/api/groupings/v2.1/groupings/hawaii.edu:custom:test:ksanidad:ksanidad-test", "delete");
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    //todo v2.2 tests (right now these endpoints just throw UnsupportedOperationException, pointless to test)

    ///////////////////////////////////////////////////////////////////////
    // MVC mapping
    //////////////////////////////////////////////////////////////////////

    // Mapping of getUserAttributes call
    private Map mapGetUserAttributes(String username) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        // Perform GET call
        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/members/" + username)
                .with(csrf()))
                .andReturn();

        // Return data if 200 OK, throw exception with status code otherwise
        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of any uri call that returns a list
    private List mapList(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of AdminsGroupings call
    private AdminListsHolder mapAdminListsHolder() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/adminsGroupings")
                .with(csrf()))
                .andReturn();
        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), AdminListsHolder.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of getGrouping call
    private Grouping mapGrouping(String groupingPath) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.1/groupings/" + groupingPath)
                .with(csrf()))
                .andReturn();
        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Grouping.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of any uri call that returns a GroupingServiceResult
    private GroupingsServiceResult mapGSR(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), GroupingsServiceResult.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of any uri call that returns a list of GroupingsServiceResults
    private List mapGSRs(String uri, String httpCall) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Helper function for mapping any uri with multiple possible HTTP call types (i.e. GET / POST / PUT / DELETE)
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