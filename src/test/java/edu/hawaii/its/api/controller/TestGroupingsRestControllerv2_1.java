package edu.hawaii.its.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.access.AnonymousUser;
import edu.hawaii.its.api.access.Role;
import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GrouperFactoryService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(TestGroupingsRestControllerv2_1.class);

    @Value("${groupings.api.test.grouping_delete}")
    private String DELETE_GROUPING;

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

    @Value("${groupings.api.test.grouping_timeout_test}")
    private String GROUPING_TIMEOUT;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("${groupings.api.test.usernames}")
    private String[] usernames;

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

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

    @Value("${groupings.api.purge_grouping}")
    private String PURGE;

    @Value("${groupings.api.person_attributes.username}")
    private String USERNAME;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.test.grouping_many_extra}")
    private String GROUPING_EXTRA;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

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

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    private MockMvc mockMvc;

    private User adminUser;
    private AnonymousUser anon;
    private User anonUser;
    private User uhUser01;
    private User uhUser02;
    private User uhUser05;
    private User uhUser03;

    private static final String API_BASE = "/api/groupings/v2.1/";

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
    public void setUp() throws IOException, MessagingException {
        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ADMIN);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[3]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[4]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[5]);

        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Creates admin user for testing
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority(Role.ADMIN.longName()));
        authorities.add(new SimpleGrantedAuthority(Role.UH.longName()));
        adminUser = new User(ADMIN, ADMIN, authorities);

        // Creates normal users for testing
        Set<GrantedAuthority> uhAuthorities = new LinkedHashSet<>();
        uhAuthorities.add(new SimpleGrantedAuthority(Role.UH.longName()));
        uhUser01 = new User(usernames[0], usernames[0], uhAuthorities);
        uhUser02 = new User(usernames[1], usernames[1], uhAuthorities);
        uhUser05 = new User(usernames[4], usernames[4], uhAuthorities);
        uhUser03 = new User(usernames[2], usernames[2], uhAuthorities);

        // Creates anonymous user for testing
        Set<GrantedAuthority> anonAuthorities = new LinkedHashSet<>();
        anonUser = new User("anonymous", anonAuthorities);
        anon = new AnonymousUser();

        // add ownership
        membershipService.addOwners(GROUPING, ADMIN, Collections.singletonList(usernames[0]));
        membershipService.addOwners(A_GROUPING, ADMIN, Collections.singletonList(usernames[4]));

        // add to include
        List<String> includeNames = new ArrayList<>();
        includeNames.add(usernames[0]);
        includeNames.add(usernames[1]);
        includeNames.add(usernames[2]);
        includeNames.add(usernames[4]);
        includeNames.add(usernames[5]);
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, includeNames);

        // remove from include
        membershipService
                .removeGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[3]));

        // add to exclude
        membershipService.addGroupMembers(usernames[0], GROUPING_EXCLUDE, Collections.singletonList(usernames[3]));

        // remove from exclude
        membershipService
                .removeGroupMembers(usernames[0], GROUPING_EXCLUDE, Collections.singletonList(usernames[2]));

        // Remove admin privileges
        membershipService.removeAdmin(ADMIN, usernames[0]);

        // Remove ownership
        membershipService.removeOwnerships(GROUPING, usernames[0], Arrays.asList(usernames));

        // Remove usernames[3] from include and add to exclude

        // Reset preferences
        groupAttributeService.changeOptInStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, usernames[0], LISTSERV, true);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, usernames[0], RELEASED_GROUPING, false);

        // Ensures users are not already in group
        membershipService.removeGroupMembers(usernames[4], A_INCLUDE, Collections.singletonList(tstUuid[0]));
        membershipService.removeGroupMembers(usernames[4], A_INCLUDE, Collections.singletonList(tstUuid[1]));
        membershipService.removeGroupMembers(usernames[4], A_INCLUDE, Collections.singletonList(tstUuid[2]));

        membershipService.removeGroupMembers(usernames[4], A_EXCLUDE, Collections.singletonList(tstUuid[0]));
        membershipService.removeGroupMembers(usernames[4], A_EXCLUDE, Collections.singletonList(tstUuid[1]));
        membershipService.removeGroupMembers(usernames[4], A_EXCLUDE, Collections.singletonList(tstUuid[2]));
    }

    @Test
    public void testConstruction() {
        assertNotNull(groupAttributeService);
        assertNotNull(groupingAssignmentService);
        assertNotNull(helperService);
        assertNotNull(memberAttributeService);
        assertNotNull(membershipService);
        assertNotNull(gc);
    }

    //todo Check status codes and all possible end cases. They may be off or different from what we expect.

    @Test
    public void adminsGroupingsFailTest() throws Exception {
        try {
            mapAdminListsHolder(uhUser01);
            fail("Shouldn't be here");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    @Test
    public void adminsGroupingsPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder(adminUser);

        // ADMIN can be replaced with any account username that has admin access
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(ADMIN));
        assertThat(listHolderPass.getAllGroupingPaths().size(), not(0));

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

        // Current user is admin.
        MvcResult result = mockMvc.perform(get(API_BASE + "members/" + usernames[0])
                .header(CURRENT_USER, ADMIN)
                .with(user(ADMIN))
                .with(csrf()))
                .andReturn();
        Map map = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertEquals("iamtst01", map.get(USERNAME));
        assertEquals("tst01name", map.get(FIRST_NAME));
        assertEquals("iamtst01", map.get(UHUUID));
        assertEquals("tst01name", map.get(COMPOSITE_NAME));
        assertEquals("tst01name", map.get(LAST_NAME));

        // Member in question is bogus.
        result = mockMvc.perform(get(API_BASE + "members/" + "bob-jones")
                .header(CURRENT_USER, ADMIN)
                .with(user(ADMIN))
                .with(csrf()))
                .andReturn();
        map = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertNull(map.get(USERNAME));
        assertNull(map.get(FIRST_NAME));
        assertNull(map.get(UHUUID));
        assertNull(map.get(COMPOSITE_NAME));
        assertNull(map.get(LAST_NAME));

        // Member in question is empty string.
        try {
            mapGetUserAttributes("", adminUser);
            result = mockMvc.perform(get(API_BASE + "members/" + "")
                    .header(CURRENT_USER, ADMIN)
                    .with(user(ADMIN))
                    .with(csrf()))
                    .andReturn();
            new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        // Current user is not owner or admin.
        result = mockMvc.perform(get(API_BASE + "members/" + usernames[0])
                .header(CURRENT_USER, usernames[2])
                .with(user(usernames[2]))
                .with(csrf()))
                .andReturn();
        map = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertNull(map.get(USERNAME));
        assertNull(map.get(FIRST_NAME));
        assertNull(map.get(UHUUID));
        assertNull(map.get(COMPOSITE_NAME));
        assertNull(map.get(LAST_NAME));

        // Current user is an owner.
        result = mockMvc.perform(get(API_BASE + "members/" + usernames[0])
                .header(CURRENT_USER, usernames[0])
                .with(user(usernames[0]))
                .with(csrf()))
                .andReturn();
        map = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertEquals("iamtst01", map.get(USERNAME));
        assertEquals("tst01name", map.get(FIRST_NAME));
        assertEquals("iamtst01", map.get(UHUUID));
        assertEquals("tst01name", map.get(COMPOSITE_NAME));
        assertEquals("tst01name", map.get(LAST_NAME));
    }

    @Test
    @WithAnonymousUser
    public void memberAttributesAnonTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "members/" + usernames[0])
                .header(CURRENT_USER, anon)
                .with(user(anon))
                .with(csrf()))
                .andReturn();
        Map map = new ObjectMapper().readValue(result.getResponse().getContentAsByteArray(), Map.class);
        assertNull(map.get(USERNAME));
        assertNull(map.get(FIRST_NAME));
        assertNull(map.get(UHUUID));
        assertNull(map.get(COMPOSITE_NAME));
        assertNull(map.get(LAST_NAME));
    }

    @Test
    public void ownerGroupingsMyselfTest() throws Exception {

        List listGroupings = mapList(API_BASE + "owners/" + usernames[0] + "/groupings", "get", uhUser01);
        assertThat(listGroupings.size(), not(0));
    }

    //    @Test
    @WithAnonymousUser
    public void ownerGroupingsAnonTest() throws Exception {

        try {
            mapList(API_BASE + "owners/" + usernames[0] + "/groupings", "get", anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    // This user owns nothing
    @Test
    public void ownerGroupingsFailTest() throws Exception {

        List<String> results = mapList(API_BASE + "owners/" + usernames[0] + "/groupings", "get", uhUser05);
        assertThat(results.size(), equalTo(0));
    }

    @Test
    public void getGroupingPassTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING, uhUser01, null, null, null, null);
        Group basis = grouping.getBasis();
        Group composite = grouping.getComposite();
        Group exclude = grouping.getExclude();
        Group include = grouping.getInclude();

        assertTrue(basis.getUsernames().contains(usernames[3]));
        assertTrue(basis.getUsernames().contains(usernames[4]));
        assertTrue(basis.getUsernames().contains(usernames[5]));
        assertTrue(basis.getNames().contains(tstName[3]));
        assertTrue(basis.getNames().contains(tstName[4]));
        assertTrue(basis.getNames().contains(tstName[5]));

        assertTrue(composite.getUsernames().contains(usernames[0]));
        assertTrue(composite.getUsernames().contains(usernames[1]));
        assertTrue(composite.getUsernames().contains(usernames[2]));
        assertTrue(composite.getUsernames().contains(usernames[4]));
        assertTrue(composite.getUsernames().contains(usernames[5]));
        assertTrue(composite.getNames().contains(tstName[0]));
        assertTrue(composite.getNames().contains(tstName[1]));
        assertTrue(composite.getNames().contains(tstName[2]));
        assertTrue(composite.getNames().contains(tstName[4]));
        assertTrue(composite.getNames().contains(tstName[5]));

        assertTrue(exclude.getUsernames().contains(usernames[3]));
        assertTrue(exclude.getNames().contains(tstName[3]));

        assertTrue(include.getUsernames().contains(usernames[0]));
        assertTrue(include.getUsernames().contains(usernames[1]));
        assertTrue(include.getUsernames().contains(usernames[2]));
        assertTrue(include.getNames().contains(tstName[0]));
        assertTrue(include.getNames().contains(tstName[1]));
        assertTrue(include.getNames().contains(tstName[2]));

        assertFalse(grouping.getOwners().getNames().contains(tstName[5]));

        try {
            mapGrouping("thisIsNotARealGrouping", uhUser01, null, null, null, null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGrouping("", uhUser01, null, null, null, null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void getGroupingFailTest() throws Exception {

        try {
            mapGrouping(GROUPING, uhUser02, null, null, null, null);
            fail("Should not be here");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    //    @Test
    @WithAnonymousUser
    public void getGroupingsAnonTest() throws Exception {

        try {
            mapGrouping(GROUPING, null, null, null, null, null);
            fail("Shouldn't be here.");
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void getPaginatedGroupingTest() throws Exception {

        // Paging starts at 1 D:
        Grouping paginatedGrouping = mapGrouping(GROUPING, uhUser01, 1, 20, "name", true);

        // Check size
        assertThat(paginatedGrouping.getBasis().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGrouping.getInclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGrouping.getExclude().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGrouping.getComposite().getMembers().size(), lessThanOrEqualTo(20));
        assertThat(paginatedGrouping.getOwners().getMembers().size(), lessThanOrEqualTo(20));

        // Check if sorted properly
        assertThat(paginatedGrouping.getBasis().getMembers().get(0).getName(), startsWith("A"));
    }

    @Test
    public void addRemoveAdminPassTest() throws Exception {

        AdminListsHolder listHolderPass = mapAdminListsHolder(adminUser);
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(usernames[0]));

        mapGSR(API_BASE + "admins/" + usernames[0], "post", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(usernames[0]));

        mapGSR(API_BASE + "admins/" + usernames[0], "post", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertTrue(listHolderPass.getAdminGroup().getUsernames().contains(usernames[0]));

        mapGSR(API_BASE + "admins/" + usernames[0], "delete", adminUser);
        listHolderPass = mapAdminListsHolder(adminUser);
        assertFalse(listHolderPass.getAdminGroup().getUsernames().contains(usernames[0]));

        try {
            mapGSR(API_BASE + "admins/bob-jones/", "post", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            listHolderPass = mapAdminListsHolder(adminUser);
            assertFalse(listHolderPass.getAdminGroup().getUsernames().contains("bob-jones"));
        }

        GroupingsServiceResult gsr = mapGSR(API_BASE + "admins/bob-jones/", "delete", adminUser);
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void addRemoveAdminFailTest() throws Exception {

        try {
            mapGSR(API_BASE + "admins/" + usernames[1], "post", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapGSR(API_BASE + "admins/" + ADMIN, "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    //    @Test
    public void addRemoveAdminAnonTest() throws Exception {

        try {
            mapGSR(API_BASE + "admins/" + usernames[0], "post", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR(API_BASE + "admins" + ADMIN, "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void addDeleteOwnerPassTest() throws Exception {

        Grouping grouping = mapGrouping(GROUPING, uhUser01, null, null, null, null);
        assertFalse(grouping.getOwners().getUsernames().contains(usernames[1]));

        mapGSR(API_BASE + "groupings/" + GROUPING + "/owners/" + usernames[1], "put", uhUser01);

        grouping = mapGrouping(GROUPING, uhUser01, null, null, null, null);
        assertTrue(grouping.getOwners().getUsernames().contains(usernames[1]));

        mapGSR(API_BASE + "groupings/" + GROUPING + "/owners/" + usernames[1], "delete", uhUser01);

        grouping = mapGrouping(GROUPING, uhUser01, null, null, null, null);
        assertFalse(grouping.getOwners().getUsernames().contains(usernames[1]));

        try {
            mapGSR(API_BASE + "groupings/someGrouping/owners/bob-jones", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSR(API_BASE + "groupings/someGrouping/owners/bob-jones", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSR(API_BASE + "groupings/someGrouping/owners/" + usernames[0], "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            List list = mapList(API_BASE + "owners/" + usernames[0] + "/groupings", "get", uhUser01);
            list.contains("someGrouping");
        }

        GroupingsServiceResult gsr =
                mapGSR(API_BASE + "groupings/" + GROUPING + "/owners/bob-jones", "delete", uhUser01);
        assertTrue(gsr.getResultCode().startsWith(SUCCESS));

        try {
            mapGSR(API_BASE + "groupings//owners//", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }

        try {
            mapGSR(API_BASE + "groupings//owners//", "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(405));
        }
    }

    @Test
    public void addDeleteOwnerFailTest() throws Exception {

        try {
            mapGSR(API_BASE + "groupings/" + GROUPING + "/owners/" + usernames[2], "put", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapGSR(API_BASE + "groupings/" + GROUPING + "/owners/" + usernames[0], "delete", uhUser02);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    @Test
    @Ignore
    @WithAnonymousUser
    public void addDeleteOwnerAnonTest() throws Exception {

        try {
            mapGSR(API_BASE + "" + GROUPING + "/owners/" + usernames[0], "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSR(API_BASE + "" + GROUPING + "/owners/" + usernames[0], "delete", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Test
    public void addDeleteOwnerUuidPassTest() throws Exception {
        assertFalse(memberAttributeService.isMember(OWNERS, tstUuid[0]));

        mapGSR(API_BASE + "groupings/" + A_GROUPING + "/owners/" + tstUuid[0], "put", uhUser05);
        assertTrue(memberAttributeService.isMember(OWNERS, tstUuid[0]));

        mapGSR(API_BASE + "groupings/" + A_GROUPING + "/owners/" + tstUuid[0], "delete", uhUser05);
        assertFalse(memberAttributeService.isMember(OWNERS, tstUuid[0]));
    }

    // todo adding an admin with a uuid is currently not supported
    @Ignore
    @Test
    public void addRemoveAdminUuidPassTest() throws Exception {
        assertFalse(memberAttributeService.isAdmin(tstUuid[0]));

        mapGSR(API_BASE + "admins/" + tstUuid[0], "post", adminUser);
        assertTrue(memberAttributeService.isAdmin(tstUuid[0]));

        mapGSR(API_BASE + "admins/" + tstUuid[0], "delete", adminUser);
        assertFalse(memberAttributeService.isAdmin(tstUuid[0]));
    }

    @Test
    public void enableDisablePreferencesAndSyncDestsPassTest() throws Exception {
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, RELEASED_GROUPING));

        mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", uhUser01);
        mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/disable", "put", uhUser01);
        mapGSR(API_BASE + "groupings/" + GROUPING + "/syncDests/" + LISTSERV + "/disable", "put", uhUser01);
        mapGSR(API_BASE + "groupings/" + GROUPING + "/syncDests/" + RELEASED_GROUPING + "/enable", "put", uhUser01);

        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, RELEASED_GROUPING));

        mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", uhUser01);
        mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/enable", "put", uhUser01);
        mapGSR(API_BASE + "groupings/" + GROUPING + "/syncDests/" + LISTSERV + "/enable", "put", uhUser01);
        mapGSR(API_BASE + "groupings/" + GROUPING + "/syncDests/" + RELEASED_GROUPING + "/disable", "put", uhUser01);

        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        assertTrue(groupAttributeService.isGroupAttribute(GROUPING, LISTSERV));
        assertFalse(groupAttributeService.isGroupAttribute(GROUPING, RELEASED_GROUPING));

        //todo Test all permutations of bad data
        try {
            mapGSRs(API_BASE + "groupings/somegrouping/preferences/nothing/enable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        try {
            mapGSRs(API_BASE + "groupings/somegrouping/preferences/nothing/disable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(501));
        }

        //todo Should maybe throw 501? I guess it doesn't care if preference name is ""
        try {
            mapGSRs(API_BASE + "groupings//preferences//enable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }

        try {
            mapGSRs(API_BASE + "groupings//preferences//disable", "put", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(404));
        }
    }

    @Test
    public void enableDisablePreferencesFailTest() throws Exception {
        //try catches have valid put calls that deliberately fail with the fail calls
        //may need future changes to the mapGSR testing
        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/enable", "put", uhUser03);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_OUT + "/disable", "put", uhUser03);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", uhUser03);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", uhUser03);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    //    @Test
    @WithAnonymousUser
    public void enableDisablePreferencesAnonTest() throws Exception {

        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/disable", "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapGSRs(API_BASE + "groupings/" + GROUPING + "/preferences/" + OPT_IN + "/enable", "put", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    @Ignore("possible feature, for now there is no way to delete a grouping.")
    @Test
    public void addDeleteGroupingFailTest() throws Exception {

        try {
            mapList(API_BASE + "groupings/" + DELETE_GROUPING, "post", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }

        try {
            mapList(API_BASE + "groupings/" + DELETE_GROUPING, "delete", uhUser01);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(403));
        }
    }

    @Ignore
    @WithAnonymousUser
    public void addDeleteGroupingAnonTest() throws Exception {

        try {
            mapList(API_BASE + "groupings/" + DELETE_GROUPING, "post", null);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }

        try {
            mapList(API_BASE + "groupings/" + DELETE_GROUPING, "delete", anon);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(302));
        }
    }

    //todo Not currently supporting this endpoint
    @Ignore
    @Test
    public void getGroupMembersTest() throws Exception {

        Group group = mapGroup(GROUPING, "basis", adminUser);
        assertThat(group.getMembers().size(), not(0));

        try {
            group = mapGroup(GROUPING_TIMEOUT, "basis", adminUser);
            fail("Shouldn't be here.");
        } catch (GroupingsHTTPException ghe) {
            assertThat(ghe.getStatusCode(), equalTo(504));
        }
    }

    //todo Not currently supporting this endpoint
    @Ignore
    @Test
    public void searchMembersTest() throws Exception {

        String path = GROUPING;
        String componentId = "basis";
        String uid = "iamtst04";

        List<LinkedHashMap> searchResults =
                mapList(API_BASE + "groupings/" + path + "/components/" + componentId + "/members/" + uid, "get",
                        adminUser);
        assertThat(searchResults.get(0).get("name"), IsEqual.equalTo("tst04name"));
        assertThat(searchResults.get(0).get("username"), IsEqual.equalTo("iamtst04"));
        assertThat(searchResults.get(0).get("uuid"), IsEqual.equalTo("iamtst04"));
    }

    //todo This tests if recursive calls break pagination, we don't need this at the moment
    @Ignore
    @Test
    public void paginatedLargeGroupingTest() throws Exception {
        recursionFunctionToTest(GROUPING_TIMEOUT, adminUser, 1, 20, "name", true);
    }

    // Helper function for paginatedLargeGroupingTest
    private void recursionFunctionToTest(String groupingPath, User user, Integer page, Integer size, String sortString,
            Boolean isAscending) throws Exception {

        if (page > 150) {
            return;
        } else {
            mapGrouping(groupingPath, user, page, size, sortString, isAscending);
            recursionFunctionToTest(groupingPath, user, page + 1, size, sortString, isAscending);

            return;
        }

    }

    //todo v2.2 tests (right now these endpoints just throw UnsupportedOperationException, pointless to test)

    ///////////////////////////////////////////////////////////////////////
    // MVC mapping
    //////////////////////////////////////////////////////////////////////

    private Map mapGetUserAttributes(String username, User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get(API_BASE + "members/" + username)
                .header(CURRENT_USER, annotationUser.getUsername())
                .with(user(annotationUser))
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Map.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
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
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of AdminsGroupings call
    private AdminListsHolder mapAdminListsHolder(User annotationUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get(API_BASE + "adminsGroupings")
                .header(CURRENT_USER, annotationUser.getUsername())
                .with(user(annotationUser))
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), AdminListsHolder.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
        }
    }

    //todo Fix for sortString and isAscending
    // Mapping of getGrouping and getPaginatedGrouping call
    private Grouping mapGrouping(String groupingPath, User currentUser, Integer page, Integer size, String sortString,
            Boolean isAscending) throws Exception {

        // Base uri string with no parameters
        String baseUri = API_BASE + "groupings/" + groupingPath + "?";

        // Add parameters based off what is or isn't null (null is non-existent param
        String params = "";
        if (page != null)
            params = params + "page=" + page;
        if (size != null) {
            if (!params.equals(""))
                params = params + "&";
            params = params + "size=" + size;
        }
        if (sortString != null) {
            if (!params.equals(""))
                params = params + "&";
            params = params + "sortString=" + sortString;
        }
        if (isAscending != null) {
            if (!params.equals(""))
                params = params + "&";
            params = params + "isAscending=" + isAscending;
        }

        // Make the API call
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get(baseUri + params)
                .header(CURRENT_USER, currentUser.getUsername())
                .with(user(currentUser))
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Grouping.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
        }
    }

    // Mapping of getGroup call
    private Group mapGroup(String parentGroupingPath, String componentId, User currentUser) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result =
                mockMvc.perform(get(API_BASE + "groupings/" + parentGroupingPath + "/components/" + componentId)
                        .header(CURRENT_USER, currentUser.getUsername())
                        .with(user(currentUser))
                        .with(csrf()))
                        .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Group.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
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
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
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
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
        }
    }

    //todo May or may not need this; saving in case
    //     Mapping of call that returns a group object asynchronously
    private Group mapAsyncGroup(String uri, User user) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(get(uri)
                .with(user(user))
                .header(CURRENT_USER, user.getUsername())
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Group.class);
        } else {
            GroupingsHTTPException ghe = new GroupingsHTTPException();
            throw new GroupingsHTTPException("URL call failed. Status code: " + result.getResponse().getStatus(),
                    ghe, result.getResponse().getStatus());
        }
    }

    // Helper function for mapping any uri with multiple possible HTTP call types (i.e. GET / POST / PUT / DELETE)
    private MvcResult mapHelper(String uri, String httpCall, User annotationUser) throws Exception {

        MvcResult result;

        switch (httpCall) {
            case "get":
                result = mockMvc.perform(get(uri)
                        .with(user(annotationUser))
                        .header(CURRENT_USER, annotationUser.getUsername())
                        .with(csrf()))
                        .andReturn();
                break;
            case "post":
                result = mockMvc.perform(post(uri)
                        .with(user(annotationUser))
                        .header(CURRENT_USER, annotationUser.getUsername())
                        .with(csrf()))
                        .andReturn();
                break;
            case "put":
                result = mockMvc.perform(put(uri)
                        .with(user(annotationUser))
                        .header(CURRENT_USER, annotationUser.getUsername())
                        .with(csrf()))
                        .andReturn();
                break;
            case "delete":
                result = mockMvc.perform(delete(uri)
                        .with(user(annotationUser))
                        .header(CURRENT_USER, annotationUser.getUsername())
                        .with(csrf()))
                        .andReturn();
                break;
            default:
                throw new IllegalArgumentException();
        }

        return result;
    }
}