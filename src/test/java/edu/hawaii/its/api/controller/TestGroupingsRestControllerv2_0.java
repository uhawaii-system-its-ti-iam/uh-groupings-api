package edu.hawaii.its.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.hawaii.its.api.access.AnonymousUser;
import edu.hawaii.its.api.access.Role;
import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.type.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingFactoryService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
public class TestGroupingsRestControllerv2_0 {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

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

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

    @Value("${grouperClient.webService.login}")
    private String APP_USER;

    @Value("${groupings.api.localhost.user}")
    private String LOCAL_USER;

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
    private User uhUser03;
    private User uhUser04;
    private User uhUser05;
    private User uhUser06;
    private User localUser;

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
        adminUser = new User(ADMIN, ADMIN, authorities);

        // Creates normal users for testing
        Set<GrantedAuthority> uhAuthorities = new LinkedHashSet<>();
        uhAuthorities.add(new SimpleGrantedAuthority(Role.UH.longName()));
        uhUser01 = new User(tst[0], tst[0], uhAuthorities);
        uhUser02 = new User(tst[1], tst[1], uhAuthorities);
        uhUser03 = new User(tst[2], tst[2], uhAuthorities);
        uhUser04 = new User(tst[3], tst[3], uhAuthorities);
        uhUser05 = new User(tst[4], tst[4], uhAuthorities);
        uhUser06 = new User(tst[5], tst[5], uhAuthorities);

        // todo username is used twice, we can switch one with a UH number if we want to
        localUser = new User(LOCAL_USER, LOCAL_USER, uhAuthorities);

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
        groupAttributeService.changeListservStatus(GROUPING, tst[0], true);

        // remove ownership
        memberAttributeService.removeOwnership(GROUPING, tst[0], tst[1]);

        // add ownership
        memberAttributeService.assignOwnership(GROUPING_STORE_EMPTY, ADMIN, tst[0]);
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

    @Test
    public void assignAndRemoveOwnershipTest() throws Exception {

        Grouping g = mapGroupingOld(GROUPING, uhUser01);

        assertFalse(g.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.0/" + GROUPING + "/" + tst[1] + "/assignOwnership", uhUser01);

        g = mapGroupingOld(GROUPING, uhUser01);

        assertTrue(g.getOwners().getUsernames().contains(tst[1]));

        mapGSR("/api/groupings/v2.0/" + GROUPING + "/" + tst[1] + "/removeOwnership", uhUser01);

        g = mapGroupingOld(GROUPING, uhUser01);

        assertFalse(g.getOwners().getUsernames().contains(tst[1]));
    }

    @Test
    public void addMemberTest() throws Exception {

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/" + tst[3] + "/addMemberToIncludeGroup", uhUser01);

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));
        //tst[3] is in basis and will go into include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));

        //add tst[3] back to exclude
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/" + tst[3] + "/addMemberToExcludeGroup", uhUser01);
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        //add tst[3] to Grouping
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/" + tst[3] + "/addGroupingMemberByUsername", uhUser01);
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));
        //tst[3] is in basis, so will not go into include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[3]));

        //todo add other test cases
    }

    @Test
    public void deleteMemberTest() throws Exception {

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));
        mapGSR("/api/groupings/v2.0/" + GROUPING + "/" + tst[3] + "/deleteMemberFromExcludeGroup", uhUser01);

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING, tst[3]));

        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[1]));
        mapGSR("/api/groupings/v2.0/" + GROUPING + "/" + tst[1] + "/deleteMemberFromIncludeGroup", uhUser01);

        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[1]));

        assertTrue(memberAttributeService.isMember(GROUPING, tst[2]));
        assertTrue(memberAttributeService.isMember(GROUPING, tst[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, tst[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/" + tst[2] + "/deleteGroupingMemberByUsername", uhUser01);
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/" + tst[5] + "/deleteGroupingMemberByUsername", uhUser01);

        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[2]));
    }

    @Test
    public void getGroupingTestOld() throws Exception {
        Grouping grouping = mapGroupingOld(GROUPING, uhUser01);
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
        mapGSR("/api/groupings/v2.0/" + grouping.getPath() + "/" + tst[5] + "/assignOwnership", uhUser01);
        grouping = mapGroupingOld(GROUPING, uhUser01);

        assertTrue(grouping.getOwners().getNames().contains(tstName[5]));
        mapGSR("/api/groupings/v2.0/" + grouping.getPath() + "/" + tst[5] + "/removeOwnership", uhUser01);
        grouping = mapGroupingOld(GROUPING, uhUser01);

        assertFalse(grouping.getOwners().getNames().contains(tstName[5]));
    }

    @Test
    public void groupingsAssignmentEmptyTest() throws Exception {
        GroupingAssignment groupings = mapGroupingAssignment(uhUser05);

        assertEquals(groupings.getGroupingsIn().size(), groupings.getGroupingsToOptOutOf().size());

        for (Grouping grouping : groupings.getGroupingsIn()) {
            mapGSRs("/api/groupings/v2.0/" + grouping.getPath() + "/optOut", uhUser05);
        }

        groupings = mapGroupingAssignment(uhUser05);

        assertEquals(0, groupings.getGroupingsIn().size());
        assertEquals(0, groupings.getGroupingsToOptOutOf().size());
    }

    @Test
    public void groupingAssignmentTest() throws Exception {
        GroupingAssignment groupings = mapGroupingAssignment(uhUser01);

        boolean inGrouping = false;
        for (Grouping grouping : groupings.getGroupingsIn()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                inGrouping = true;
                break;
            }
        }
        assertTrue(inGrouping);

        boolean canOptin = false;
        for (Grouping grouping : groupings.getGroupingsToOptInTo()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                canOptin = true;
                break;
            }
        }
        assertFalse(canOptin);

        boolean canOptOut = false;
        for (Grouping grouping : groupings.getGroupingsToOptOutOf()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                canOptOut = true;
                break;
            }
        }
        assertTrue(canOptOut);

        boolean ownsGrouping = false;
        for (Grouping grouping : groupings.getGroupingsOwned()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                ownsGrouping = true;
                break;
            }
        }
        assertTrue(ownsGrouping);

    }

    @Test
    public void myGroupingsTest2() throws Exception {
        GroupingAssignment groupings = mapGroupingAssignment(uhUser04);

        boolean inGrouping = false;
        for (Grouping grouping : groupings.getGroupingsIn()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                inGrouping = true;
                break;
            }
        }
        assertFalse(inGrouping);

        boolean ownsGrouping = false;
        for (Grouping grouping : groupings.getGroupingsOwned()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                ownsGrouping = true;
                break;
            }
        }
        assertFalse(ownsGrouping);
    }

    @Test
    public void myGroupingsTest3() throws Exception {
        boolean optedIn = false;

        GroupingAssignment tst4Groupings = mapGroupingAssignment(uhUser04);
        assertEquals(tst4Groupings.getGroupingsOptedInTo().size(), 0);
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/optIn", uhUser04);
        tst4Groupings = mapGroupingAssignment(uhUser06);
        for (Grouping grouping : tst4Groupings.getGroupingsOptedInTo()) {
            if (grouping.getPath().contains(GROUPING)) {
                optedIn = true;
            }
        }
        //in basis
        assertFalse(optedIn);
    }

    @Test
    public void myGroupingsTest4() throws Exception {
        boolean optedOut = false;

        GroupingAssignment tst5Groupings = mapGroupingAssignment(uhUser06);
        assertEquals(tst5Groupings.getGroupingsOptedOutOf().size(), 0);
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/optOut", uhUser06);
        tst5Groupings = mapGroupingAssignment(uhUser06);

        for (Grouping grouping : tst5Groupings.getGroupingsOptedOutOf()) {
            if (grouping.getPath().contains(this.GROUPING)) {
                optedOut = true;
            }
        }
        assertTrue(optedOut);

        membershipService.deleteGroupMemberByUsername(tst[0], GROUPING_EXCLUDE, tst[5]);
    }

    @Test
    public void optInTest() throws Exception {
        //tst[3] is not in Grouping, but is in basis and exclude
        assertFalse(memberAttributeService.isMember(GROUPING, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, tst[3]));

        //tst[3] opts into Grouping
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/optIn", uhUser04);

        //tst[3] is now in composite, still in basis and not in exclude
        assertTrue(memberAttributeService.isMember(GROUPING, tst[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, tst[3]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, tst[3]));
    }

    @Test
    public void optOutTest() throws Exception {
        //tst[5] is in the Grouping and in the basis
        assertTrue(memberAttributeService.isMember(GROUPING, tst[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, tst[5]));

        //tst[5] opts out of Grouping
        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/optOut", uhUser06);

        //tst[5] is now in exclude, not in include or Grouping
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, tst[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, tst[5]));
        assertFalse(memberAttributeService.isMember(GROUPING, tst[5]));
    }

    @Test
    public void changeListservStatusTest() throws Exception {
        assertTrue(groupAttributeService.hasListserv(GROUPING));

        mapGSR("/api/groupings/v2.0/" + GROUPING + "/false/setListserv", uhUser01);

        assertFalse(groupAttributeService.hasListserv(GROUPING));

        mapGSR("/api/groupings/v2.0/" + GROUPING + "/true/setListserv", uhUser01);
        assertTrue(groupAttributeService.hasListserv(GROUPING));
    }

    @Test
    public void changeOptInTest() throws Exception {
        assertTrue(groupAttributeService.optInPermission(GROUPING));

        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/false/setOptIn", uhUser01);
        assertFalse(groupAttributeService.optInPermission(GROUPING));

        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/true/setOptIn", uhUser01);
        assertTrue(groupAttributeService.optInPermission(GROUPING));
    }

    @Test
    public void changeOptOutTest() throws Exception {
        assertTrue(groupAttributeService.optOutPermission(GROUPING));

        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/false/setOptOut", uhUser01);
        assertFalse(groupAttributeService.optOutPermission(GROUPING));

        mapGSRs("/api/groupings/v2.0/" + GROUPING + "/true/setOptOut", uhUser01);
        assertTrue(groupAttributeService.optOutPermission(GROUPING));
    }

    @Test
    public void aaronTest() throws Exception {
        //This test often fails because the test server is very slow.
        //Because the server caches some results and gets quicker the more times
        //it is run, we let it run a few times if it starts failing

        int i = 0;
        while (i < 5) {
            try {
                GroupingAssignment aaronsGroupings = mapGroupingAssignment(localUser);
                assertNotNull(aaronsGroupings);
                break;
            } catch (AssertionError ae) {
                i++;
            }
        }
        assertTrue(i < 5);
    }

    @Test
    public void getEmptyGroupingTest() throws Exception {

        assertTrue(memberAttributeService.isOwner(GROUPING_STORE_EMPTY, tst[0]));
        Grouping storeEmpty = mapGroupingOld(GROUPING_STORE_EMPTY, uhUser01);
        Grouping trueEmpty = mapGroupingOld(GROUPING_TRUE_EMPTY, uhUser01);

        assertTrue(storeEmpty.getBasis().getMembers().size() == 0);
        assertTrue(storeEmpty.getComposite().getMembers().size() == 0);
        assertTrue(storeEmpty.getExclude().getMembers().size() == 0);
        assertTrue(storeEmpty.getInclude().getMembers().size() == 0);
        assertTrue(storeEmpty.getOwners().getUsernames().contains(tst[0]));

        assertTrue(trueEmpty.getBasis().getMembers().size() == 0);
        assertTrue(trueEmpty.getComposite().getMembers().size() == 0);
        assertTrue(trueEmpty.getExclude().getMembers().size() == 0);
        assertTrue(trueEmpty.getInclude().getMembers().size() == 0);
        assertTrue(trueEmpty.getOwners().getUsernames().contains(tst[0]));

    }

    @Test
    public void adminListsFailTest() throws Exception {
        AdminListsHolder infoFail = mapAdminListsHolderOld(uhUser01);

        assertEquals(infoFail.getAdminGroup().getMembers().size(), 0);
        assertEquals(infoFail.getAllGroupings().size(), 0);
    }

    @Test
    public void adminListsPassTest() throws Exception {
        AdminListsHolder infoSuccess = mapAdminListsHolderOld(adminUser);

        //ADMIN can be replaced with any account username that has admin access
        assertTrue(infoSuccess.getAdminGroup().getUsernames().contains(ADMIN));
    }

    @Test
    public void addDeleteAdminTestOld() throws Exception {
        GroupingsServiceResult addAdminResults;
        GroupingsServiceResult deleteAdminResults;

        try {
            //            addAdminResults = gc.addAdmin(tst[0], tst[0]).getBody();
            addAdminResults = mapGSR("/api/groupings/v2.0/" + tst[0] + "/addAdmin", uhUser01);
        } catch (GroupingsHTTPException ghe) {
            addAdminResults = new GroupingsServiceResult();
            addAdminResults.setResultCode(FAILURE);
        }

        try {
            //            deleteAdminResults = gc.deleteAdmin(tst[0], tst[0]).getBody();
            deleteAdminResults = mapGSR("/api/groupings/v2.0/" + tst[0] + "/deleteAdmin", uhUser01);
        } catch (GroupingsHTTPException ghe) {
            deleteAdminResults = new GroupingsServiceResult();
            deleteAdminResults.setResultCode(FAILURE);
        }

        assertTrue(addAdminResults.getResultCode().startsWith(FAILURE));
        assertTrue(deleteAdminResults.getResultCode().startsWith(FAILURE));
    }

    //////////////////////////////////
    //    2.0 REST API Mappings     //
    //////////////////////////////////

    private Grouping mapGroupingOld(String groupingPath, User current_user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.0/" + groupingPath + "/grouping")
                .header(CURRENT_USER, current_user.getUsername()))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Grouping.class);
    }

    private GroupingsServiceResult mapGSR(String uri, User current_user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(post(uri)
                .header(CURRENT_USER, current_user.getUsername())
                .with(csrf()))
                .andReturn();

        if (result.getResponse().getStatus() == 200) {
            return objectMapper.readValue(result.getResponse().getContentAsByteArray(), GroupingsServiceResult.class);
        } else {
            throw new GroupingsHTTPException();
        }
    }

    private List mapGSRs(String uri, User current_user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(post(uri)
                .header(CURRENT_USER, current_user.getUsername())
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), List.class);
    }

    private GroupingAssignment mapGroupingAssignment(User current_user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.0/groupingAssignment")
                .header(CURRENT_USER, current_user.getUsername())
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), GroupingAssignment.class);
    }

    private AdminListsHolder mapAdminListsHolderOld(User current_user) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult result = mockMvc.perform(get("/api/groupings/v2.0/adminLists")
                .header(CURRENT_USER, current_user.getUsername())
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), AdminListsHolder.class);
    }
}
