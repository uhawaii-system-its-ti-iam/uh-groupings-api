package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.SyncDestination;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsRestControllerv2_1Test {

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

    @BeforeEach
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
    @WithMockUhUser
    public void helloTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), is("University of Hawaii Groupings"));
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
        mockMvc.perform(get(API_BASE + "/admins-and-groupings")
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

    @Test
    @WithMockUhUser(username = "admin")
    public void addAdminTest() throws Exception {
        String adminToAdd = "adminToAdd";
        AddMemberResult addMemberResult = new AddMemberResult();
        given(membershipService.addAdmin(ADMIN, adminToAdd)).willReturn(addMemberResult);
        mockMvc.perform(post(API_BASE + "/admins/" + adminToAdd)
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());
        verify(membershipService, times(1))
                .addAdmin(ADMIN, adminToAdd);
    }

    @Test
    @WithMockUhUser(username = "admin")
    public void removeAdminTest() throws Exception {
        String adminToRemove = "adminToRemove";
        RemoveMemberResult removeMemberResult = new RemoveMemberResult();
        given(membershipService.removeAdmin(ADMIN, adminToRemove))
                .willReturn(removeMemberResult);

        mockMvc.perform(delete(API_BASE + "/admins/" + adminToRemove)
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeAdmin(ADMIN, adminToRemove);
    }

    @Test
    @WithMockUhUser
    public void removeFromGroupsTest() throws Exception {
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add("grouping" + i);
        }
        String userToRemove = "userToRemove";
        given(membershipService.removeFromGroups(ADMIN, userToRemove, paths)).willReturn(removeMemberResults);
        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/" + String.join(",", paths) + "/" + userToRemove)
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1)).removeFromGroups(ADMIN, userToRemove, paths);

    }

    @Test
    @WithMockUhUser
    public void resetGroupTest() throws Exception {
        List<RemoveMemberResult> removeMemberResults = new ArrayList<>();
        List<String> includePaths = new ArrayList<>();
        List<String> excludePaths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            includePaths.add("groupingInclude" + i);
            excludePaths.add("groupingExclude" + i);
        }
        String includePathsStr = String.join(",", includePaths);
        String excludePathsStr = String.join(",", excludePaths);
        given(membershipService.resetGroup(ADMIN, "grouping", includePaths, excludePaths)).willReturn(
                removeMemberResults);
        MvcResult result =
                mockMvc.perform(
                        delete(API_BASE + "/groupings/grouping/" + includePathsStr + "/" + excludePathsStr
                                + "/reset-group")
                                        .with(csrf())
                                        .header(CURRENT_USER, ADMIN))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1)).resetGroup(ADMIN, "grouping", includePaths, excludePaths);
    }

    @Test
    @WithMockUhUser
    public void memberAttributesTest() throws Exception {
        MvcResult validResult = mockMvc.perform(get(API_BASE + "/members/i0-uuid")
                .header(CURRENT_USER, "0o0-username"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(validResult, notNullValue());

        MvcResult invalidResult = mockMvc.perform(get(API_BASE + "/members/<h1>hello<h1>")
                .header(CURRENT_USER, "0o0-username"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(invalidResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void getGrouping() throws Exception {
        given(groupingAssignmentService.getPaginatedGrouping(GROUPING, USERNAME, 1, 1, "name", true))
                .willReturn(grouping());

        mockMvc.perform(
                get(API_BASE + "/groupings/" + GROUPING + "?page=1&size=1&sortString=name&isAscending=true")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk()).andReturn();

        verify(groupingAssignmentService, times(1))
                .getPaginatedGrouping(GROUPING, USERNAME, 1, 1, "name", true);
    }

    @Test
    @WithMockUhUser
    public void membershipResultsTest() throws Exception {
        List<Membership> memberships = new ArrayList<>();
        given(membershipService.membershipResults(ADMIN, "iamtst01")).willReturn(memberships);

        mockMvc.perform(get(API_BASE + "/members/iamtst01/groupings")
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .membershipResults(ADMIN, "iamtst01");
    }

    @Test
    @WithMockUhUser
    public void getOptInGroupingPathsTest() throws Exception {
        List<GroupingPath> optInGroupingPaths = new ArrayList<>();
        given(groupingAssignmentService.optInGroupingPaths(ADMIN, "iamtst01")).willReturn(optInGroupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings/members/iamtst01/opt-in-groups")
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(groupingAssignmentService, times(1))
                .optInGroupingPaths(ADMIN, "iamtst01");
    }

    @Test
    @WithMockUhUser
    public void optInTest() throws Exception {
        MvcResult includeResult =
                mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/include-members/o6-username/self")
                        .header("current_user", "o6-username")
                        .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(includeResult, notNullValue());
    }

    @Test
    @WithMockUhUser
    public void optOutTest() throws Exception {
        MvcResult excludeResult =
                mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/exclude-members/o6-username/self")
                        .header("current_user", "o6-username")
                        .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(excludeResult, notNullValue());
    }

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
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members/" + String.join(",", usersToAdd))
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addIncludeMembers(USERNAME, "grouping", usersToAdd);
    }

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

        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members/" + String.join(",", usersToAdd))
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addExcludeMembers(USERNAME, "grouping", usersToAdd);
    }

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
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/include-members/" + String.join(",", usersToRemove))
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeIncludeMembers(USERNAME, "grouping", usersToRemove);
    }

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
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude-members/" + String.join(",", usersToRemove))
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeExcludeMembers(USERNAME, "grouping", usersToRemove);
    }

    @Test
    @WithMockUhUser(username = "bobo")
    public void ownerGroupingsTest() throws Exception {
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

    @Test
    @WithMockUhUser
    public void addOwnersTest() throws Exception {
        List<String> ownersToAdd = new ArrayList<>();
        List<AddMemberResult> addMemberResultList = new ArrayList<>();
        ownersToAdd.add("tst04name");
        ownersToAdd.add("tst05name");
        ownersToAdd.add("tst06name");

        given(membershipService.addOwnerships("grouping", USERNAME, ownersToAdd))
                .willReturn(addMemberResultList);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToAdd))
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(membershipService, times(1))
                .addOwnerships("grouping", USERNAME, ownersToAdd);

    }

    @Test
    @WithMockUhUser
    public void removeOwnersTest() throws Exception {
        List<String> ownersToRemove = new ArrayList<>();
        List<RemoveMemberResult> removeMemberResultList = new ArrayList<>();
        ownersToRemove.add("tst04name");
        ownersToRemove.add("tst05name");
        ownersToRemove.add("tst06name");

        given(membershipService.removeOwnerships("grouping", USERNAME, ownersToRemove))
                .willReturn(removeMemberResultList);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToRemove))
                        .with(csrf())
                        .header(CURRENT_USER, USERNAME))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1))
                .removeOwnerships("grouping", USERNAME, ownersToRemove);
    }

    @Test
    @WithMockUhUser
    public void updateDescriptionTest() throws Exception {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();

        given(groupAttributeService.updateDescription("grouping", USERNAME, "description")).willReturn(
                groupingsServiceResult);
        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/description")
                .with(csrf())
                .header(CURRENT_USER, USERNAME)
                .content("description"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);

        verify(groupAttributeService, times(1))
                .updateDescription("grouping", USERNAME, "description");
    }

    @Test
    @WithMockUhUser
    public void enablePreferenceSyncDestTest() throws Exception {

        OptRequest optInRequest = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withUsername(USERNAME)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptValue(true)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withUsername(USERNAME)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptValue(true)
                .build();

        given(groupAttributeService.changeOptStatus(optInRequest, optOutRequest)).willReturn(gsrListIn());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preference/" + OptType.IN.value() + "/enable")
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-in"));
        verify(groupAttributeService, times(1)).changeOptStatus(optInRequest, optOutRequest);

        optInRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withUsername(USERNAME)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withUsername(USERNAME)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptValue(false)
                .build();

        given(groupAttributeService.changeOptStatus(optInRequest, optOutRequest)).willReturn(gsrListOut());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preference/" + OptType.OUT.value() + "/disable")
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-out"));
        verify(groupAttributeService, times(1)).changeOptStatus(optInRequest, optOutRequest);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true))
                .willReturn(gsrListserv());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + LISTSERV + "/enable")
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("listserv status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true))
                .willReturn(gsrReleasedGrouping());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + RELEASED_GROUPING + "/enable")
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("ldap status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true);
    }

    @Test
    @WithMockUhUser
    public void getSyncDestinationsTest() throws Exception {
        given(groupAttributeService.getAllSyncDestinations(USERNAME, "grouping"))
                .willReturn(sdList());

        mockMvc.perform(get(API_BASE + "/groupings/grouping/sync-destinations")
                .with(csrf())
                .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(groupAttributeService, times(1))
                .getAllSyncDestinations(USERNAME, "grouping");
    }

    @Test
    @WithAnonymousUser
    public void hasOwnerPrivsTest() throws Exception {
        given(memberAttributeService.isOwner(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/owners")
                .with(csrf())
                .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberAttributeService, times(1))
                .isOwner(CURRENT_USER);
    }

    @Test
    @WithAnonymousUser
    public void hasAdminPrivsTest() throws Exception {
        given(memberAttributeService.isAdmin(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/admins")
                .with(csrf())
                .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberAttributeService, times(1))
                .isAdmin(CURRENT_USER);
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
    public void syncDestinationsTest() throws Exception {
        Grouping group = groupingTwo();
        System.out.println(group.getOwners());

        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/" + groupingTwo().getPath() + "/sync-destinations")
                .header("current_user", "o6-username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    @WithMockUhUser(username = "iamtst01")
    public void getNumberOfMembershipsTest() throws Exception {
        String uid = currentUser().getUid();
        given(membershipService.getNumberOfMemberships(ADMIN, uid))
                .willReturn(369);

        mockMvc.perform(get(API_BASE + "/groupings/members/" + uid + "/memberships")
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("369"));

        verify(membershipService, times(1))
                .getNumberOfMemberships(ADMIN, uid);
    }

    @Test
    @WithMockUhUser
    public void isSoleOwnerTest() throws Exception {
        String uid = "uid";
        String path = "grouping-path";
        given(groupingAssignmentService.isSoleOwner(ADMIN, path, uid)).willReturn(true);
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/" + path + "/owners/" + uid)
                .with(csrf())
                .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).isSoleOwner(ADMIN, path, uid);
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
}
