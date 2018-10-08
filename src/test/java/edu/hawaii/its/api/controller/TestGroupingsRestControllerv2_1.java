package edu.hawaii.its.api.controller;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.*;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import edu.hawaii.its.api.access.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Value ("${groupings.api.test.grouping_delete}")
    private String DELETE_GROUPING;

    @Value ("${groupings.api.test.grouping_delete_test}")
    private String DELETE_GROUPING_TEST;

    @Value("${grouperClient.webService.login}")
    private String APP_USER;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_custom}")
    private String A_GROUPING;

    @Value("${groupings.api.test.grouping_custom_include}")
    private String A_INCLUDE;

    @Value("${groupings.api.test.grouping_custom_exclude}")
    private String A_EXCLUDE;

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

    @Value("${groupings.api.test.grouping_custom_owners}")
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

    private User adminUser;
    private AnonymousUser anon;
    private User anonUser;
    private User uhUser01;
    private User uhUser02;
    private User uhUser05;
    private User uhUser03;

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

        // Creates admin user for testing
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority(Role.ADMIN.longName()));
        authorities.add(new SimpleGrantedAuthority(Role.UH.longName()));
        adminUser = new User(APP_USER, APP_USER, authorities);

        // Creates normal users for testing
        Set<GrantedAuthority> uhAuthorities = new LinkedHashSet<>();
        uhAuthorities.add(new SimpleGrantedAuthority(Role.UH.longName()));
        uhUser01= new User("iamtst01", "iamtst01", uhAuthorities);
        uhUser02= new User("iamtst02", "iamtst02", uhAuthorities);
        uhUser05= new User("iamtst05", "iamtst05", uhAuthorities);
        uhUser03 = new User("iamtst03", "iamtst03", uhAuthorities);

        // Creates anonymous user for testing
        Set<GrantedAuthority> anonAuthorities = new LinkedHashSet<>();
        anonUser = new User("anonymous", anonAuthorities);
        anon = new AnonymousUser();

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
        try {
            groupingFactoryService.deleteGrouping(ADMIN, "hawaii.edu:custom:test:ksanidad:bw-test");
        } catch (GroupingsServiceResultException gsre) {
            logger.info("Grouping doesn't exist.");
        }

        // Initialize test uuids
        tstUuid[0] = "10976564";
        tstUuid[1] = "11077773";
        tstUuid[2] = "11077784";

        // Ensures users are not already in group
        membershipService.deleteGroupMemberByUsername("iamtst05", A_INCLUDE, tstUuid[0]);
        membershipService.deleteGroupMemberByUsername("iamtst05", A_INCLUDE, tstUuid[1]);
        membershipService.deleteGroupMemberByUsername("iamtst05", A_INCLUDE, tstUuid[2]);

        membershipService.deleteGroupMemberByUsername("iamtst05", A_EXCLUDE, tstUuid[0]);
        membershipService.deleteGroupMemberByUsername("iamtst05", A_EXCLUDE, tstUuid[1]);
        membershipService.deleteGroupMemberByUsername("iamtst05", A_EXCLUDE, tstUuid[2]);
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

    @Test
    public void adminsGroupingsFailTest() throws Exception {

        AdminListsHolder listHolderFail = mapAdminListsHolder(uhUser01);

        assertThat(listHolderFail.getAdminGroup().getMembers().size(), equalTo(0));
        assertThat(listHolderFail.getAllGroupings().size(), equalTo(0));
    }

    @Test
    public void adminsGroupingsPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder(adminUser);

        // ADMIN can be replaced with any account username that has admin access
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(ADMIN));
        assertThat(listHolderPass.getAllGroupings().size(), not(0));

    }

    // Anonymous user (not logged in)
//    @Test
    @WithAnonymousUser
    public void adminsGroupingsAnonTest() throws Exception {

        try {
            mapAdminListsHolder(anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void memberAttributesAdminTest() throws Exception {

        Map attributes = mapGetUserAttributes(tst[0], adminUser);

        assertThat(attributes.get("uid"), equalTo("iamtst01"));
        assertThat(attributes.get("givenName"), equalTo("tst01name"));
        assertThat(attributes.get("uhuuid"), equalTo("iamtst01"));
        assertThat(attributes.get("cn"), equalTo("tst01name"));
        assertThat(attributes.get("sn"), equalTo("tst01name"));

        try {
            mapGetUserAttributes("bob-jones", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGetUserAttributes("", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void memberAttributesMyselfTest() throws Exception {

        Map attributes = mapGetUserAttributes(tst[0], uhUser01);

        assertThat(attributes.get("uid"), equalTo("iamtst01"));
        assertThat(attributes.get("givenName"), equalTo("tst01name"));
        assertThat(attributes.get("uhuuid"), equalTo("iamtst01"));
        assertThat(attributes.get("cn"), equalTo("tst01name"));
        assertThat(attributes.get("sn"), equalTo("tst01name"));

    }

    // This user owns nothing
    @Test
    public void memberAttributesFailTest() throws Exception {

        Map attributes = mapGetUserAttributes(tst[0], uhUser03);

        assertThat(attributes.get("uid"), equalTo(""));
        assertThat(attributes.get("givenName"), equalTo(""));
        assertThat(attributes.get("uhuuid"), equalTo(""));
        assertThat(attributes.get("cn"), equalTo(""));
        assertThat(attributes.get("sn"), equalTo(""));

        //        assertThat(mapGetUserAttributes(tst[0]).size(), equalTo(0));
        //        try {
        //            mapGetUserAttributes(tst[0]);
        //            fail("Shouldn't be here.");
        //        } catch (GroupingsHTTPException ghe) {
        //            assertThat(ghe.getStatusCode(), equalTo(403));
        //        }
    }

//    @Test
    @WithAnonymousUser
    public void memberAttributesAnonTest() throws Exception {

        try {
            mapGetUserAttributes(tst[0], anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void memberGroupingsAdminTest() throws Exception {

        List listMemberships = mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get", adminUser);
        assertThat(listMemberships.size(), not(0));

        try {
            mapList("/api/groupings/v2.1/members/bob-jones/groupings", "get", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapList("/api/groupings/v2.1/members//groupings", "get", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void memberGroupingsMyselfTest() throws Exception {

        List listMemberships = mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get", uhUser01);
        assertThat(listMemberships.size(), not(0));
    }

//    @Test
    @WithAnonymousUser
    public void memberGroupingsAnonTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get", anonUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    // This user owns nothing
    @Test
    public void memberGroupingsFailTest() throws Exception {

        List<String> results = mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get", uhUser05);
        assertThat(results.size(), equalTo(0));
        //        try {
        //            mapList("/api/groupings/v2.1/members/" + tst[0] + "/groupings", "get");
        //            fail("Shouldn't be here.");
        //        } catch (GroupingsHTTPException ghe) {
        //            assertThat(ghe.getStatusCode(), equalTo(403));
        //        }
    }

    @Test
    public void ownerGroupingsAdminTest() throws Exception {

        List listGroupings = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get", adminUser);
        assertThat(listGroupings.size(), not(0));

        try {
            mapList("/api/groupings/v2.1/owners/bob-jones/groupings", "get", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapList("/api/groupings/v2.1/owners//groupings", "get", adminUser);
            fail("Shouldn't be here");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void ownerGroupingsMyselfTest() throws Exception {

        List listGroupings = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get", uhUser01);
        assertThat(listGroupings.size(), not(0));
    }

//    @Test
    @WithAnonymousUser
    public void ownerGroupingsAnonTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get", anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    // This user owns nothing
    @Test
    public void ownerGroupingsFailTest() throws Exception {

        List<String> results = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get", uhUser05);
        assertThat(results.size(), equalTo(0));
    }

    @Test
    public void getGroupingPassTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING, uhUser01);
        Group basis = grouping.getBasis();
        Group composite = grouping.getComposite();
        Group exclude = grouping.getExclude();
        Group include = grouping.getInclude();

        assertTrue(basis.getUsernames().contains(tst[3]));
        assertTrue(basis.getUsernames().contains(tst[4]));
        assertTrue(basis.getUsernames().contains(tst[5]));
        assertTrue(basis.getNames().contains(tstName[3]));
        assertTrue(basis.getNames().contains(tstName[4]));
        assertTrue(basis.getNames().contains(tstName[5]));

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

        assertTrue(exclude.getUsernames().contains(tst[3]));
        assertTrue(exclude.getNames().contains(tstName[3]));

        assertTrue(include.getUsernames().contains(tst[0]));
        assertTrue(include.getUsernames().contains(tst[1]));
        assertTrue(include.getUsernames().contains(tst[2]));
        assertTrue(include.getNames().contains(tstName[0]));
        assertTrue(include.getNames().contains(tstName[1]));
        assertTrue(include.getNames().contains(tstName[2]));

        assertFalse(grouping.getOwners().getNames().contains(tstName[5]));

        try {
            mapGrouping("thisIsNotARealGrouping", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGrouping("", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void getGroupingFailTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING, uhUser02);
        assertThat(grouping.getBasis().getUsernames().size(), equalTo(0));
        assertThat(grouping.getInclude().getUsernames().size(), equalTo(0));
        assertThat(grouping.getExclude().getUsernames().size(), equalTo(0));
        assertThat(grouping.getComposite().getUsernames().size(), equalTo(0));
    }

//    @Test
    @WithAnonymousUser
    public void getGroupingsAnonTest() throws Exception {

        try {
            mapGrouping(GROUPING, null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void addDeleteAdminPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder(adminUser);
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tst[0], "delete", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(tst[0]));

        try {
            mapGSR("/api/groupings/v2.1/admins/bob-jones/", "post", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            listHolderPass = mapAdminListsHolder(adminUser);
            assertFalse(listHolderPass.getAdminGroup().getUsernames().contains("bob-jones"));
        }

        try {
            mapGSR("/api/groupings/v2.1/admins//", "post", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            listHolderPass = mapAdminListsHolder(adminUser);
            assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(""));
        }

        GroupingsServiceResult gsr = mapGSR("/api/groupings/v2.1/admins/bob-jones/", "delete", adminUser);
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void addDeleteAdminFailTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/admins/" + tst[1], "post", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSR("/api/groupings/v2.1/admins/" + ADMIN, "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

//    @Test
    public void addDeleteAdminAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/admins/" + tst[0], "post", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/admins" + ADMIN, "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void addDeleteOwnerPassTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING, uhUser01);
        assertFalse(grouping.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[1], "put", uhUser01);

        grouping = mapGrouping(GROUPING, uhUser01);
        assertTrue(grouping.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[1], "delete", uhUser01);

        grouping = mapGrouping(GROUPING, uhUser01);
        assertFalse(grouping.getOwners().getUsernames().contains(tst[1]));

        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bob-jones", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/bob-jones", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/someGrouping/owners/" + tst[0], "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            List list = mapList("/api/groupings/v2.1/owners/" + tst[0] + "/groupings", "get", uhUser01);
            list.contains("someGrouping");
        }

        GroupingsServiceResult gsr = mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/bob-jones", "delete", uhUser01);
        gsr.getResultCode().startsWith(SUCCESS);

        try {
            mapGSR("/api/groupings/v2.1/groupings//owners//", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings//owners//", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    public void addDeleteOwnerFailTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[2], "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/owners/" + tst[0], "delete", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

//    @Test
    @WithAnonymousUser
    public void addDeleteOwnerAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/owners/" + tst[0], "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/owners/" + tst[0], "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }


    @Test
    public void addDeleteMemberUuidPassTest() throws Exception {

        assertFalse(memberAttributeService.isMember(A_INCLUDE, tstUuid[0]));
        assertFalse(memberAttributeService.isMember(A_EXCLUDE, tstUuid[0]));
        assertFalse(memberAttributeService.isMember(A_INCLUDE, tstUuid[1]));
        assertFalse(memberAttributeService.isMember(A_EXCLUDE, tstUuid[1]));

        mapGSRs("/api/groupings/v2.1/groupings/" + A_GROUPING + "/includeMembers/" + tstUuid[0], "put", uhUser05);

        assertTrue(memberAttributeService.isMember(A_INCLUDE, tstUuid[0]));
        assertFalse(memberAttributeService.isMember(A_EXCLUDE, tstUuid[0]));

        mapGSRs("/api/groupings/v2.1/groupings/" + A_GROUPING + "/excludeMembers/" + tstUuid[1], "put", uhUser05);
        assertFalse(memberAttributeService.isMember(A_INCLUDE, tstUuid[1]));
        assertTrue(memberAttributeService.isMember(A_EXCLUDE, tstUuid[1]));

        mapGSR("/api/groupings/v2.1/groupings/" + A_GROUPING + "/includeMembers/" + tstUuid[0], "delete", uhUser05);
        assertFalse(memberAttributeService.isMember(A_INCLUDE, tstUuid[0]));

        mapGSR("/api/groupings/v2.1/groupings/" + A_GROUPING + "/excludeMembers/" + tstUuid[1], "delete", uhUser05);
        assertFalse(memberAttributeService.isMember(A_EXCLUDE, tst[1]));
    }

    @Test
    public void addDeleteOwnerUuidPassTest() throws Exception {
        assertFalse(memberAttributeService.isMember(OWNERS, tstUuid[0]));

        mapGSR("/api/groupings/v2.1/groupings/" + A_GROUPING + "/owners/" + tstUuid[0], "put", uhUser05);
        assertTrue(memberAttributeService.isMember(OWNERS, tstUuid[0]));

        mapGSR("/api/groupings/v2.1/groupings/" + A_GROUPING + "/owners/" + tstUuid[0], "delete", uhUser05);
        assertFalse(memberAttributeService.isMember(OWNERS, tstUuid[0]));
    }

    @Test
    public void addDeleteAdminUuidPassTest() throws Exception {
        assertFalse(memberAttributeService.isAdmin(tstUuid[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tstUuid[0], "post", adminUser);
        assertTrue(memberAttributeService.isAdmin(tstUuid[0]));

        mapGSR("/api/groupings/v2.1/admins/" + tstUuid[0], "delete", adminUser);
        assertFalse(memberAttributeService.isAdmin(tstUuid[0]));
    }

    @Test
    public void addDeleteMemberPassTest() throws Exception {
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "put", uhUser01);

        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "delete", uhUser01);

        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "put", uhUser01);

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));

        mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "delete", uhUser01);

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[2]));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[2], "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[3], "put", uhUser01);

        // Garbage data tests
        //todo Test all permutations of bad data
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bob-jones", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/includeMembers/bob-jones", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bob-jones", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/excludeMembers/bob-jones", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Empty fields tests
        try {
            mapGSRs("/api/groupings/v2.1/groupings//includeMembers//", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//includeMembers//", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//excludeMembers//", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//excludeMembers//", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

    @Test
    public void addDeleteMemberFailTest() throws Exception {

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[3], "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/includeMembers/" + tst[2], "delete", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[2], "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSR("/api/groupings/v2.1/groupings/" + GROUPING + "/excludeMembers/" + tst[3], "delete", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

//    @Test
    @WithAnonymousUser
    public void addDeleteMemberAnonTest() throws Exception {

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/includeMembers/" + tst[2], "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/includeMembers/" + tst[2], "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/excludeMembers/" + tst[2], "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR("/api/groupings/v2.1/" + GROUPING + "/excludeMembers/" + tst[2], "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void enableDisablePreferencesPassTest() throws Exception {
        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasReleasedGrouping(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/disable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/disable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + RELEASED_GROUPING + "/enable", "put", uhUser01);

        assertFalse(groupAttributeService.optInPermission(GROUPING));
        assertFalse(groupAttributeService.optOutPermission(GROUPING));
        assertFalse(groupAttributeService.hasListserv(GROUPING));
        assertTrue(groupAttributeService.hasReleasedGrouping(GROUPING));

        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/enable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + LISTSERV + "/enable", "put", uhUser01);
        mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + RELEASED_GROUPING + "/disable", "put", uhUser01);

        assertTrue(groupAttributeService.optInPermission(GROUPING));
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
        assertTrue(groupAttributeService.hasListserv(GROUPING));
        assertFalse(groupAttributeService.hasReleasedGrouping(GROUPING));

        //todo Test all permutations of bad data
        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/enable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/somegrouping/preferences/nothing/disable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        //todo Should maybe throw 501? I guess it doesn't care if preference name is ""
        try {
            mapGSRs("/api/groupings/v2.1/groupings//preferences//enable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings//preferences//disable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void enableDisablePreferencesFailTest() throws Exception {
        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }


//    @Test
    @WithAnonymousUser
    public void enableDisablePreferencesAnonTest() throws Exception {

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSRs("/api/groupings/v2.1/groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void addDeleteGroupingPassTest() throws Exception {
        String newGrouping = DELETE_GROUPING;

        try {
            mapGrouping(newGrouping, adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "post", adminUser);

        try {
            mapGrouping(newGrouping, adminUser);
        } catch (GroupingsHTTPException ghe) {
            fail("Shouldn't be here.");
        }

        mapList("/api/groupings/v2.1/groupings/" + newGrouping, "delete", adminUser);

        //todo Might need to refactor depending on outcome of deleteGrouping
        try {
            mapGrouping(newGrouping, adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void addDeleteGroupingFailTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/groupings/" + DELETE_GROUPING, "post", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }

        try {
            mapList("/api/groupings/v2.1/groupings/" + DELETE_GROUPING_TEST , "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(400));
        }
    }

//    @Test
    @WithAnonymousUser
    public void addDeleteGroupingAnonTest() throws Exception {

        try {
            mapList("/api/groupings/v2.1/groupings/" + DELETE_GROUPING, "post", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapList("/api/groupings/v2.1/groupings/" + DELETE_GROUPING_TEST, "delete", anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    //todo v2.2 tests (right now these endpoints just throw UnsupportedOperationException, pointless to test)

    ///////////////////////////////////////////////////////////////////////
    // MVC mapping
    //////////////////////////////////////////////////////////////////////

    private Map mapGetUserAttributes(String username, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = null;

        result = mockMvc.perform(get("/api/groupings/v2.1/members/" + username)
                .with(user(annotationUser))
                .with(csrf()))
                .andReturn();


        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of any uri call that returns a list
    private List mapList(String uri, String httpCall, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall, annotationUser);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }


    // Mapping of AdminsGroupings call
    private AdminListsHolder mapAdminListsHolder(User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = null;

        result = mockMvc.perform(get("/api/groupings/v2.1/adminsGroupings")
                .with(user(annotationUser))
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
    private Grouping mapGrouping(String groupingPath, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = null;
        User currentUser = annotationUser;

        result = mockMvc.perform(get("/api/groupings/v2.1/groupings/" + groupingPath)
                .with(user(currentUser))
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
    private GroupingsServiceResult mapGSR(String uri, String httpCall, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall, annotationUser);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), GroupingsServiceResult.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of any uri call that returns a list of GroupingsServiceResults
    private List mapGSRs(String uri, String httpCall, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mapHelper(uri, httpCall, annotationUser);

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed.", ghe, result.getResponse().getStatus());
        }
    }

    // Helper function for mapping any uri with multiple possible HTTP call types (i.e. GET / POST / PUT / DELETE)
    private MvcResult mapHelper(String uri, String httpCall, User annotationUser) throws Exception {

        MvcResult result;

        switch (httpCall) {
            case "get":
                result = mockMvc.perform(get(uri)
                    .with(user(annotationUser))
                    .with(csrf()))
                    .andReturn();
                break;
            case "post":
                result = mockMvc.perform(post(uri)
                    .with(user(annotationUser))
                    .with(csrf()))
                    .andReturn();
                break;
            case "put":
                result = mockMvc.perform(put(uri)
                     .with(user(annotationUser))
                     .with(csrf()))
                     .andReturn();
                break;
            case "delete":
                result = mockMvc.perform(delete(uri)
                     .with(user(annotationUser))
                     .with(csrf()))
                     .andReturn();
                break;
            default:
                throw new IllegalArgumentException();
        }

        return result;
    }
}