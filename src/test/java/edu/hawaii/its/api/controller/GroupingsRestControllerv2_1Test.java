package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.SyncDestination;

@RunWith(SpringRunner.class)
@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsRestControllerv2_1Test {

    @Value("${app.iam.request.form}")
    private String requestForm;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @MockBean
    private GroupAttributeService groupAttributeService;

    @MockBean
    private GroupingAssignmentService groupingAssignmentService;

    @MockBean
    private HelperService helperService;

    @MockBean
    private MemberAttributeService memberAttributeService;

    @MockBean
    private MembershipService membershipService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "grouping";
    private static final String USERNAME = "user";
    private static final String ADMIN = "admin";

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private User currentUser() {
        return userContextService.getCurrentUser();
    }

    // Test data.
    private Grouping grouping() {
        Grouping grouping = new Grouping("test:ing:me:bob");

        Group basisGroup = new Group();
        basisGroup.addMember(new Person("b0-name", "b0-uuid", "b0-username"));
        basisGroup.addMember(new Person("b1-name", "b1-uuid", "b1-username"));
        basisGroup.addMember(new Person("b2-name", "b2-uuid", "b2-username"));
        grouping.setBasis(basisGroup);

        Group exclude = new Group();
        exclude.addMember(new Person("e0-name", "e0-uuid", "e0-username"));
        grouping.setExclude(exclude);

        Group include = new Group();
        include.addMember(new Person("i0-name", "i0-uuid", "i0-username"));
        include.addMember(new Person("i1-name", "i1-uuid", "i1-username"));
        grouping.setInclude(include);

        Group owners = new Group();
        owners.addMember(new Person("o0-name", "o0-uuid", "o0-username"));
        owners.addMember(new Person("o1-name", "o1-uuid", "o1-username"));
        owners.addMember(new Person("o2-name", "o2-uuid", "o2-username"));
        owners.addMember(new Person("o3-name", "o3-uuid", "o3-username"));
        grouping.setOwners(owners);

        grouping.changeSyncDestinationState(LISTSERV, true);

        return grouping;
    }

    // Test data.
    private Grouping groupingTwo() {
        Grouping grouping = new Grouping("test:ing:me:kim");

        Group basisGroup = new Group();
        basisGroup.addMember(new Person("b4-name", "b4-uuid", "b4-username"));
        basisGroup.addMember(new Person("b5-name", "b5-uuid", "b5-username"));
        basisGroup.addMember(new Person("b6-name", "b6-uuid", "b6-username"));
        grouping.setBasis(basisGroup);

        Group exclude = new Group();
        exclude.addMember(new Person("e4-name", "e4-uuid", "e4-username"));
        grouping.setExclude(exclude);

        Group include = new Group();
        include.addMember(new Person("i4-name", "i4-uuid", "i4-username"));
        include.addMember(new Person("i5-name", "i5-uuid", "i5-username"));
        grouping.setInclude(include);

        Group owners = new Group();
        owners.addMember(new Person("o4-name", "o4-uuid", "o4-username"));
        owners.addMember(new Person("o5-name", "o5-uuid", "o5-username"));
        owners.addMember(new Person("o6-name", "o6-uuid", "o6-username"));
        owners.addMember(new Person("o7-name", "o7-uuid", "o7-username"));
        grouping.setOwners(owners);

        grouping.changeSyncDestinationState(LISTSERV, true);

        return grouping;
    }

    // Test data (2.1 API).
    private List<String> groupingStringList() {
        List<String> mockGroupingList = new ArrayList<>();

        mockGroupingList.add("g0-gName");
        mockGroupingList.add("g1-gName");
        mockGroupingList.add("g2-gName");

        return mockGroupingList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrList() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "add users to grouping"));
        return gsrList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrList2() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "remove member from include group"));
        return gsrList;
    }

    private List<SyncDestination> sdList() {
        List<SyncDestination> sdList = new ArrayList<>();
        sdList.add(new SyncDestination(SUCCESS, "retrieved new sync destinations"));
        return sdList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrListIn() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "member is opted-in"));
        return gsrList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrListIn2() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "member is not opted-in"));
        return gsrList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrListOut() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "member is opted-out"));
        return gsrList;
    }

    //Test data (2.1 API).
    private List<GroupingsServiceResult> gsrListOut2() {
        List<GroupingsServiceResult> gsrList = new ArrayList<>();
        gsrList.add(new GroupingsServiceResult(SUCCESS, "member is not opted-out"));
        return gsrList;
    }

    //Test data (2.1 API).
    private GroupingsServiceResult gsrListserv() {
        return new GroupingsServiceResult(SUCCESS, "listserv status changed");
    }

    //Test data (2.1 API).
    private GroupingsServiceResult gsrReleasedGrouping() {
        return new GroupingsServiceResult(SUCCESS, "ldap status changed");
    }

    @Test
    @WithMockUhUser(username = "bobo")
    public void adminsGroupingsTest() throws Exception {
        List<GroupingPath> groupingPaths = new ArrayList<>();
        List<Person> admins = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            groupingPaths.add(new GroupingPath("path:to:grouping" + i));
            admins.add(new Person("admin" + i));
        }
        Group adminGroup = new Group(admins);
        AdminListsHolder adminListsHolder = new AdminListsHolder(groupingPaths, adminGroup);

        given(groupingAssignmentService.adminLists("bobo")).willReturn(adminListsHolder);
        mockMvc.perform(get(API_BASE + "/adminsGroupings")
                        .header(CURRENT_USER, "bobo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("allGroupingPaths[0].name").value("grouping0"))
                .andExpect(jsonPath("allGroupingPaths[1].name").value("grouping1"))
                .andExpect(jsonPath("allGroupingPaths[2].name").value("grouping2"))
                .andExpect(jsonPath("allGroupingPaths[0].path").value("path:to:grouping0"))
                .andExpect(jsonPath("allGroupingPaths[1].path").value("path:to:grouping1"))
                .andExpect(jsonPath("allGroupingPaths[2].path").value("path:to:grouping2"))
                .andExpect(jsonPath("adminGroup.members[0].name").value("admin0"))
                .andExpect(jsonPath("adminGroup.members[1].name").value("admin1"))
                .andExpect(jsonPath("adminGroup.members[2].name").value("admin2"));
        verify(groupingAssignmentService, times(1))
                .adminLists("bobo");
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonAdminsGroupingsTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/adminsGroupings"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    //todo GetUserAttributes has no tests(?)
    //todo As Admin

    //todo As Myself

    //todo As Nobody


    //todo This user owns nothing
    //    @Test
    //    @WithMockUhUser(username = "")
    //    public void memberGroupingsFailTest() throws Exception {
    //        mockMvc.perform(get(API_BASE + "/members/grouping/groupings"))
    //                .andExpect(status().is4xxClientError());
    //    }

    //    @WithAnonymousUser
    public void memberGroupingsAnonTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/members/grouping/groupings"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser(username = "bobo")
    public void getNumberOfGroupingsTest() throws Exception {
        final String uid = "grouping";
        final String owner = "bobo";

        String path = "grouping";

        List<GroupingPath> groupingPathList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            groupingPathList.add(new GroupingPath(path));
        }
        given(memberAttributeService.getNumberOfGroupings(owner, uid)).willReturn(10);

        mockMvc.perform(get(API_BASE + "/owners/grouping/grouping")
                        .header(CURRENT_USER, owner))
                .andExpect(status().isOk());
        verify(memberAttributeService, times(1))
                .getNumberOfGroupings(owner, uid);
    }

    @Test
    @WithMockUhUser(username = "bobo")
    public void getGroupingsOwnedAdminTest() throws Exception {
        final String uid = "grouping";
        final String admin = "bobo";

        String path = "path:to:grouping";

        List<GroupingPath> groupingPathList = new ArrayList<>();
        groupingPathList.add(new GroupingPath(path));

        given(memberAttributeService.getOwnedGroupings(admin, uid))
                .willReturn(groupingPathList);
        mockMvc.perform(get(API_BASE + "/owners/grouping/groupings")
                        .header(CURRENT_USER, admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value(path))
                .andExpect(jsonPath("$[0].name").value("grouping"));

        verify(memberAttributeService, times(1))
                .getOwnedGroupings(admin, uid);
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonOwnerGroupingsTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/owners/grouping/groupings"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser(username = "uhAdmin")
    public void addNewAdminTest() throws Exception {
        String admin = "uhAdmin";
        given(membershipService.addAdmin(admin, "newAdmin"))
                .willReturn(new GroupingsServiceResult(SUCCESS, "add admin"));

        mockMvc.perform(post(API_BASE + "/admins/newAdmin")
                        .with(csrf())
                        .header(CURRENT_USER, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value(SUCCESS))
                .andExpect(jsonPath("action").value("add admin"));

        verify(membershipService, times(1))
                .addAdmin(admin, "newAdmin");
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonAddAdminTest() throws Exception {
        MvcResult result = mockMvc.perform(post(API_BASE + "/admins/newAdmin").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser(username = "uhAdmin")
    public void addOwnerTest() throws Exception {
        List<AddMemberResult> returnList = new ArrayList<>();
        String admin = "uhAdmin";

        given(membershipService.addOwners("path1", "uhAdmin", Collections.singletonList("newOwner")))
                .willReturn(returnList);

        mockMvc.perform(put(API_BASE + "/groupings/path1/owners/newOwner")
                        .with(csrf())
                        .header(CURRENT_USER, admin))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addOwners("path1", "uhAdmin", Collections.singletonList("newOwner"));
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonAddOwnerTest() throws Exception {
        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/path1/owners/newOwner").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void swaggerToStringTest() throws Exception {
        given(helperService.swaggerToString(ADMIN)).willReturn(new GenericServiceResult());
        mockMvc.perform(get(API_BASE + "/swagger/toString/")
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(helperService, times(1))
                .swaggerToString(ADMIN);
    }

    @Test
    @WithMockUhUser
    public void membershipResultsTest() throws Exception {
        List<Membership> memberships = new ArrayList<>();
        given(memberAttributeService.getMembershipResults(ADMIN, "iamtst01")).willReturn(memberships);

        mockMvc.perform(get(API_BASE + "/members/iamtst01/groupings")
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(memberAttributeService, times(1))
                .getMembershipResults(ADMIN, "iamtst01");
    }

    @Test
    @WithMockUhUser
    public void getSyncDestinationsTest() throws Exception {
        given(groupAttributeService.getAllSyncDestinations(USERNAME, "grouping"))
                .willReturn(sdList());

        mockMvc.perform(get(API_BASE + "/groupings/grouping/syncDestinations")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(groupAttributeService, times(1))
                .getAllSyncDestinations(USERNAME, "grouping");
    }

    @Test
    @WithMockUhUser
    public void getOptInGroupsTest() throws Exception {
        List<String> optInGroups = new ArrayList<>();
        given(groupingAssignmentService.getOptInGroups(ADMIN, "iamtst01")).willReturn(optInGroups);
        mockMvc.perform(get(API_BASE + "/groupings/optInGroups/iamtst01")
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(groupingAssignmentService, times(1))
                .getOptInGroups(ADMIN, "iamtst01");
    }

    @Ignore
    @Test
    @WithMockUhUser
    public void addIncludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(membershipService.addIncludeMembers(USERNAME, "grouping", usersToAdd))
                .willReturn(addMemberResults);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/includeMembers/" + usersToAdd)
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addIncludeMembers(USERNAME, "grouping", usersToAdd);
    }

    @Ignore
    @Test
    @WithMockUhUser
    public void addExcludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        List<AddMemberResult> addMemberResults = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(membershipService.addExcludeMembers(USERNAME, "grouping", usersToAdd))
                .willReturn(addMemberResults);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/excludeMembers/" + usersToAdd)
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addExcludeMembers(USERNAME, "grouping", usersToAdd);
    }

    @Ignore
    @Test
    @WithMockUhUser
    public void removeIncludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(membershipService.removeIncludeMembers(USERNAME, "grouping", usersToRemove))
                .willReturn(removeMemberResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/includeMembers/" + usersToRemove)
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeIncludeMembers(USERNAME, "grouping", usersToRemove);
    }

    @Ignore
    @Test
    @WithMockUhUser
    public void removeExcludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(membershipService.removeExcludeMembers(USERNAME, "grouping", usersToRemove))
                .willReturn(removeMemberResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/excludeMembers/" + usersToRemove)
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeExcludeMembers(USERNAME, "grouping", usersToRemove);
    }

    @Test
    @WithMockUhUser
    public void enablePreferenceSyncDestTest() throws Exception {
        given(groupAttributeService.changeOptInStatus("grouping", USERNAME, true))
                .willReturn(gsrListIn());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_IN + "/enable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-in"));

        verify(groupAttributeService, times(1))
                .changeOptInStatus("grouping", USERNAME, true);

        given(groupAttributeService.changeOptOutStatus("grouping", USERNAME, true))
                .willReturn(gsrListOut());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_OUT + "/enable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-out"));

       verify(groupAttributeService, times(1))
               .changeOptOutStatus("grouping", USERNAME, true);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true))
                .willReturn(gsrListserv());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + LISTSERV + "/enable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("listserv status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true))
                .willReturn(gsrReleasedGrouping());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + RELEASED_GROUPING + "/enable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("ldap status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true);
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonEnablePreferenceSyncDestTest() throws Exception {
        MvcResult inResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_IN + "/enable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(inResult, notNullValue());

        MvcResult outResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_OUT + "/enable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(outResult, notNullValue());

        MvcResult listResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + LISTSERV + "/enable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(listResult, notNullValue());

        MvcResult groupingResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + RELEASED_GROUPING + "/enable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(groupingResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void disablePreferenceSyncDestTest() throws Exception {
        given(groupAttributeService.changeOptInStatus("grouping", USERNAME, false))
                .willReturn(gsrListIn2());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_IN + "/disable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is not opted-in"));

        verify(groupAttributeService, times(1))
                .changeOptInStatus("grouping", USERNAME, false);

        given(groupAttributeService.changeOptOutStatus("grouping", USERNAME, false))
                .willReturn(gsrListOut2());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_OUT + "/disable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is not opted-out"));

        verify(groupAttributeService, times(1))
                .changeOptOutStatus("grouping", USERNAME, false);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, false))
                .willReturn(gsrListserv());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + LISTSERV + "/disable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("listserv status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, false);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, false))
                .willReturn(gsrReleasedGrouping());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + RELEASED_GROUPING + "/disable")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("ldap status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, false);
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonDisablePreferenceSyncDestTest() throws Exception {
        MvcResult inResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_IN + "/disable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(inResult, notNullValue());

        MvcResult outResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/preferences/" + OPT_OUT + "/disable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(outResult, notNullValue());

        MvcResult listResult = mockMvc.perform(put(API_BASE + "/groupings/grouping/syncDests/" + LISTSERV + "/disable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(listResult, notNullValue());

        MvcResult groupingResult = mockMvc.perform(
                        put(API_BASE + "/groupings/grouping/syncDests/" + RELEASED_GROUPING + "/disable").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(groupingResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void removeNewAdminTest() throws Exception {
        given(membershipService.removeAdmin(USERNAME, "homerSimpson"))
                .willReturn(new GroupingsServiceResult(SUCCESS, "removed admin"));

        mockMvc.perform(delete(API_BASE + "/admins/homerSimpson")
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value(SUCCESS))
                .andExpect(jsonPath("action").value("removed admin"));

        verify(membershipService, times(1))
                .removeAdmin(USERNAME, "homerSimpson");
    }

    @Test
    @WithMockUhUser
    public void removeFromGroupsTest() throws Exception {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add("grouping" + i);
        }
        String userToRemove = "homerSimpson";
        // Look into adding given and verify calls for this test
        /*given(membershipService.removeAdmin(ADMIN, ))
                .willReturn(new GroupingsServiceResult(SUCCESS, "removed admin"));*/

        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/" + paths + "/" + userToRemove)
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonRemoveAdminTest() throws Exception {
        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/homerSimpson").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void removeOwnerTest() throws Exception {
        List<String> ownersToRemove = new ArrayList<>();
        List<RemoveMemberResult> removeMemberResultList = new ArrayList<>();
        ownersToRemove.add("tst04name");
        ownersToRemove.add("tst05name");
        ownersToRemove.add("tst06name");

        given(membershipService.removeOwnerships("grouping", USERNAME, ownersToRemove))
                .willReturn(removeMemberResultList);

        MvcResult result = mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/" + ownersToRemove)
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
    }

    @Ignore
    @Test
    @WithAnonymousUser
    public void anonRemoveOwnerTest() throws Exception {
        MvcResult result = mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/frye")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void rootTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString(), is("University of Hawaii Groupings"));
    }

    @Test
    @WithMockUhUser
    public void getGrouping() throws Exception {
        given(groupingAssignmentService.getPaginatedGrouping(GROUPING, USERNAME, null, null, null, null))
                .willReturn(grouping());

        mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING)
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("bob"))
                .andExpect(jsonPath("path").value("test:ing:me:bob"))
                .andExpect(jsonPath("syncDestinations").isEmpty())
                .andExpect(jsonPath("basis.members", hasSize(3)))
                .andExpect(jsonPath("basis.members[0].name").value("b0-name"))
                .andExpect(jsonPath("basis.members[0].uhUuid").value("b0-uuid"))

                .andExpect(jsonPath("basis.members[0].username").value("b0-username"))
                .andExpect(jsonPath("basis.members[1].name").value("b1-name"))
                .andExpect(jsonPath("basis.members[1].uhUuid").value("b1-uuid"))
                .andExpect(jsonPath("basis.members[1].username").value("b1-username"))
                .andExpect(jsonPath("basis.members[2].name").value("b2-name"))
                .andExpect(jsonPath("basis.members[2].uhUuid").value("b2-uuid"))
                .andExpect(jsonPath("basis.members[2].username").value("b2-username"))
                .andExpect(jsonPath("exclude.members", hasSize(1)))
                .andExpect(jsonPath("exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("exclude.members[0].name").value("e0-name"))
                .andExpect(jsonPath("exclude.members[0].uhUuid").value("e0-uuid"))
                .andExpect(jsonPath("include.members", hasSize(2)))
                .andExpect(jsonPath("include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("include.members[1].name").value("i1-name"))
                .andExpect(jsonPath("include.members[1].uhUuid").value("i1-uuid"))
                .andExpect(jsonPath("owners.members", hasSize(4)))
                .andExpect(jsonPath("owners.members[3].name").value("o3-name"))
                .andExpect(jsonPath("owners.members[3].uhUuid").value("o3-uuid"))
                .andExpect(jsonPath("owners.members[3].username").value("o3-username"))
                .andExpect(jsonPath("composite.members", hasSize(0)));

        verify(groupingAssignmentService, times(1))
                .getPaginatedGrouping(GROUPING, USERNAME, null, null, null, null);
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void addAdminTest() throws Exception {
        String newAdmin = "newAdmin";
        given(membershipService.addAdmin(ADMIN, newAdmin))
                .willReturn(new GroupingsServiceResult(SUCCESS, "add " + newAdmin));

        mockMvc.perform(post(API_BASE + "/admins/" + newAdmin)
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value(SUCCESS))
                .andExpect(jsonPath("action").value("add " + newAdmin));

        verify(membershipService, times(1))
                .addAdmin(ADMIN, newAdmin);
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void removeAdminTest() throws Exception {
        String adminToRemove = "adminToRemove";
        given(membershipService.removeAdmin(ADMIN, adminToRemove))
                .willReturn(new GroupingsServiceResult(SUCCESS, "removed admin"));

        mockMvc.perform(delete(API_BASE + "/admins/" + adminToRemove)
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultCode").value(SUCCESS))
                .andExpect(jsonPath("action").value("removed admin"));

        verify(membershipService, times(1))
                .removeAdmin(ADMIN, adminToRemove);
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void adminListsTest() throws Exception {
        String mvcResult = mockMvc.perform(get(API_BASE + "/adminsGroupings")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(mvcResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void descriptionCheck() throws Exception {
        Grouping testGrouping = groupingTwo();
        String mockGroupPath = testGrouping.getPath();

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/" + mockGroupPath + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Test")
                        .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void memberLookupTest() throws Exception {
        MvcResult validResult = mockMvc.perform(get(API_BASE + "/members/i0-uuid")
                        .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(validResult, notNullValue());

        MvcResult invalidResult = mockMvc.perform(get(API_BASE + "/members/<h1>hello<h1>")
                        .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(invalidResult, notNullValue());
    }

    @Ignore
    @Test
    @WithMockUhUser(username = "abc")
    public void lookUpPermissionTestMember() throws Exception {
        MvcResult ownerResult = mockMvc.perform(get(API_BASE + "/owners/" + USERNAME + "/groupings")
                .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(ownerResult, notNullValue());

        MvcResult groupingsResult = mockMvc.perform(get(API_BASE + "/members/" + USERNAME + "/groupings")
                .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(groupingsResult, notNullValue());

        MvcResult memberAttributeResult = mockMvc.perform(get(API_BASE + "/members/" + USERNAME)
                .header(CURRENT_USER, "0o0-username"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(memberAttributeResult, notNullValue());
    }

    @Ignore
    @Test
    @WithMockAdminUser(username = "bobo")
    public void lookUpPermissionTestAdmin() throws Exception {
        String newAdmin = "newAdmin";
        given(membershipService.addAdmin(ADMIN, newAdmin))
                .willReturn(new GroupingsServiceResult(SUCCESS, "add " + newAdmin));

        mockMvc.perform(get(API_BASE + "/owners/" + USERNAME + "/groupings")
                        .header(CURRENT_USER, newAdmin))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get(API_BASE + "/members/" + USERNAME + "/groupings")
                        .header(CURRENT_USER, newAdmin))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get(API_BASE + "/members/" + USERNAME)
                        .header(CURRENT_USER, newAdmin))
                .andDo(print())
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addAdmin(ADMIN, newAdmin);
    }

    @Ignore
    @Test
    @WithMockUhUser(username = "testUser")
    public void lookUpPermissionTestOwner() throws Exception {
        Grouping testGroup = grouping();
        System.out.println("TEST GROUP: " + testGroup);
        System.out.println("OWNERS: " + testGroup.getOwners());
        System.out.println("THE PATH: " + testGroup.getPath());

        // Try to look up information about member in owned group <-- SUCCEED

        // Create an unrelated group
        // Try to look up information about member in unrelated group <-- FAIL

        // Keeps failing; is this a bug?
        assertTrue(memberAttributeService.isOwner(testGroup.getPath(), "o0-username"));

        //            MvcResult ownerResult = mockMvc.perform(get(API_BASE + "/owners/" + lookUp[i] + "/groupings"))
        //                    .andDo(print())
        //                    .andExpect(status().isOk())
        //                    .andReturn();
        //
        //            MvcResult groupingsResult = mockMvc.perform(get(API_BASE + "/members/" + lookUp[i] + "/groupings"))
        //                    .andDo(print())
        //                    .andExpect(status().isOk())
        //                    .andReturn();
        //
        //            MvcResult memberAttributeResult = mockMvc.perform(get(API_BASE + "/members/" + lookUp[i]))
        //                    .andDo(print())
        //                    .andExpect(status().isOk())
        //                    .andReturn();
    }

    @Test
    @WithMockUhUser(username = "abc")
    public void descriptionTest() throws Exception {
        Grouping groupingTest = grouping();

        // Check that regular member cannot change description
        MvcResult memberDescriptionResult =
                mockMvc.perform(get(API_BASE + "/groupings/" + groupingTest.getPath() + "/description")
                                .header(CURRENT_USER, "abc"))
                        .andDo(print())
                        .andExpect(status().is4xxClientError())
                        .andReturn();
        assertThat(memberDescriptionResult, notNullValue());

        // Admin should be able to change description
        MvcResult adminDescriptionResult =
                mockMvc.perform(put(API_BASE + "/groupings/" + groupingTest.getPath() + "/description")
                                .header(CURRENT_USER, "admin"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(adminDescriptionResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void selfTest() throws Exception {

        MvcResult includeResult = mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/includeMembers/o6-username/self")
                        .header("current_user", "o6-username")
                        .header("accept", "application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(includeResult, notNullValue());

        MvcResult excludeResult = mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/excludeMembers/o6-username/self")
                        .header("current_user", "o6-username")
                        .header("accept", "application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(excludeResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void syncDestinationsTest() throws Exception {
        Grouping group = groupingTwo();
        System.out.println(group.getOwners());

        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/syncDestinations")
                        .header("current_user", "o6-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void regexTest() throws Exception {
        // Sending an 'unsafe character' in the URI should get rejected and return CLIENT_ERROR
        MvcResult result1 = mockMvc.perform(get(API_BASE + "/owners/" + USERNAME + "[" + "/groupings")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result1, notNullValue());

        MvcResult result2 = mockMvc.perform(get(API_BASE + "/owners/" + USERNAME + "^" + "/groupings")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result2, notNullValue());

        MvcResult result3 = mockMvc.perform(get(API_BASE + "/members/" + USERNAME + "}")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result3, notNullValue());

        MvcResult result4 = mockMvc.perform(get(API_BASE + "/members/" + USERNAME + "@")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertThat(result4, notNullValue());
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void getNumberOfMembershipsTest() throws Exception {
        String uid = currentUser().getUid();
        given(memberAttributeService.getNumberOfMemberships(ADMIN, uid))
                .willReturn(369);

        mockMvc.perform(get(API_BASE + "/groupings/" + uid + "/memberships")
                        .with(csrf())
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("369"));

        verify(memberAttributeService, times(1))
                .getNumberOfMemberships(ADMIN, uid);
    }
}