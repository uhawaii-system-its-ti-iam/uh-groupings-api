package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
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
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.type.UIAddMemberResults;
import edu.hawaii.its.api.type.UIRemoveMemberResults;
import edu.hawaii.its.api.util.JsonUtil;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingsRestControllerv2_1Test {

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
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "grouping";
    private static final String USERNAME = "user";
    private static final String ADMIN = "admin";

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
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
    public void helloTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), is("University of Hawaii Groupings"));
    }

    @Test
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
    public void addAdminTest() throws Exception {
        String adminToAdd = "adminToAdd";
        GroupingsAddResult addMemberResult = new GroupingsAddResult();
        given(membershipService.addAdmin(ADMIN, adminToAdd)).willReturn(addMemberResult);
        mockMvc.perform(post(API_BASE + "/admins/" + adminToAdd)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());
        verify(membershipService, times(1))
                .addAdmin(ADMIN, adminToAdd);
    }

    @Test
    public void removeAdminTest() throws Exception {
        String adminToRemove = "adminToRemove";
        GroupingsRemoveResult removeMemberResult = new GroupingsRemoveResult();
        given(membershipService.removeAdmin(ADMIN, adminToRemove))
                .willReturn(removeMemberResult);

        mockMvc.perform(delete(API_BASE + "/admins/" + adminToRemove)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeAdmin(ADMIN, adminToRemove);
    }

    @Test
    public void removeFromGroupsTest() throws Exception {
        List<UIRemoveMemberResults> removeMemberResults = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add("grouping" + i);
        }
        String userToRemove = "userToRemove";
        given(membershipService.removeFromGroups(ADMIN, userToRemove, paths)).willReturn(removeMemberResults);
        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/" + String.join(",", paths) + "/" + userToRemove)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1)).removeFromGroups(ADMIN, userToRemove, paths);

    }

    @Test
    public void resetGroupTest() throws Exception {
        List<UIRemoveMemberResults> removeMemberResults = new ArrayList<>();
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
                                        .header(CURRENT_USER, ADMIN))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1)).resetGroup(ADMIN, "grouping", includePaths, excludePaths);
    }

    @Test
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
    public void membershipResultsTest() throws Exception {
        List<Membership> memberships = new ArrayList<>();
        given(membershipService.membershipResults(ADMIN, "iamtst01")).willReturn(memberships);

        mockMvc.perform(get(API_BASE + "/members/iamtst01/memberships")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .membershipResults(ADMIN, "iamtst01");
    }

    @Test
    public void managePersonResultsTest() throws Exception {
        List<Membership> results = new ArrayList<>();
        given(membershipService.managePersonResults(ADMIN, "iamtst01")).willReturn(results);

        mockMvc.perform(get(API_BASE + "/members/iamtst01/groupings")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .managePersonResults(ADMIN, "iamtst01");
    }

    @Test
    public void getOptInGroupingPathsTest() throws Exception {
        List<GroupingPath> optInGroupingPaths = new ArrayList<>();
        given(groupingAssignmentService.optInGroupingPaths(ADMIN, "iamtst01")).willReturn(optInGroupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings/members/iamtst01/opt-in-groups")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(groupingAssignmentService, times(1))
                .optInGroupingPaths(ADMIN, "iamtst01");
    }

    @Test
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
    public void addIncludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        List<UIAddMemberResults> addMemberResults = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(membershipService.addIncludeMembers(USERNAME, "grouping", usersToAdd))
                .willReturn(addMemberResults);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members/")
                        .header(CURRENT_USER, USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addIncludeMembers(USERNAME, "grouping", usersToAdd);
    }

    @Test
    public void addExcludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        List<UIAddMemberResults> addMemberResults = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(membershipService.addExcludeMembers(USERNAME, "grouping", usersToAdd))
                .willReturn(addMemberResults);

        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members/")
                        .header(CURRENT_USER, USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .addExcludeMembers(USERNAME, "grouping", usersToAdd);
    }

    @Test
    public void removeIncludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        List<UIRemoveMemberResults> removeMemberResults = new ArrayList<>();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(membershipService.removeIncludeMembers(USERNAME, "grouping", usersToRemove))
                .willReturn(removeMemberResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/include-members/")
                        .header(CURRENT_USER, USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeIncludeMembers(USERNAME, "grouping", usersToRemove);
    }

    @Test
    public void removeExcludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        List<UIRemoveMemberResults> removeMemberResults = new ArrayList<>();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(membershipService.removeExcludeMembers(USERNAME, "grouping", usersToRemove))
                .willReturn(removeMemberResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude-members/")
                        .header(CURRENT_USER, USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .removeExcludeMembers(USERNAME, "grouping", usersToRemove);
    }

    @Test
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
    public void addOwnersTest() throws Exception {
        List<String> ownersToAdd = new ArrayList<>();
        List<UIAddMemberResults> addMemberResultList = new ArrayList<>();
        ownersToAdd.add("tst04name");
        ownersToAdd.add("tst05name");
        ownersToAdd.add("tst06name");

        given(membershipService.addOwnerships("grouping", USERNAME, ownersToAdd))
                .willReturn(addMemberResultList);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToAdd))
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(membershipService, times(1))
                .addOwnerships("grouping", USERNAME, ownersToAdd);

    }

    @Test
    public void removeOwnersTest() throws Exception {
        List<String> ownersToRemove = new ArrayList<>();
        List<UIRemoveMemberResults> removeMemberResultList = new ArrayList<>();
        ownersToRemove.add("tst04name");
        ownersToRemove.add("tst05name");
        ownersToRemove.add("tst06name");

        given(membershipService.removeOwnerships("grouping", USERNAME, ownersToRemove))
                .willReturn(removeMemberResultList);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToRemove))
                                .header(CURRENT_USER, USERNAME))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(membershipService, times(1))
                .removeOwnerships("grouping", USERNAME, ownersToRemove);
    }

    @Test
    public void updateDescriptionTest() throws Exception {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();

        given(groupAttributeService.updateDescription("grouping", USERNAME, "description")).willReturn(
                groupingsServiceResult);
        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/description")
                        .header(CURRENT_USER, USERNAME)
                        .content("description"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);

        verify(groupAttributeService, times(1))
                .updateDescription("grouping", USERNAME, "description");
    }

    @Test
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
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-out"));
        verify(groupAttributeService, times(1)).changeOptStatus(optInRequest, optOutRequest);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true))
                .willReturn(gsrListserv());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + LISTSERV + "/enable")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("listserv status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, LISTSERV, true);

        given(groupAttributeService.changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true))
                .willReturn(gsrReleasedGrouping());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + RELEASED_GROUPING + "/enable")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("ldap status changed"));

        verify(groupAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", USERNAME, RELEASED_GROUPING, true);
    }

    @Test
    public void getSyncDestinationsTest() throws Exception {
        given(groupAttributeService.getAllSyncDestinations(USERNAME, "grouping"))
                .willReturn(sdList());

        mockMvc.perform(get(API_BASE + "/groupings/grouping/sync-destinations")
                        .header(CURRENT_USER, USERNAME))
                .andExpect(status().isOk());

        verify(groupAttributeService, times(1))
                .getAllSyncDestinations(USERNAME, "grouping");
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        given(memberAttributeService.isOwner(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/owners")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberAttributeService, times(1))
                .isOwner(CURRENT_USER);
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        given(memberAttributeService.isAdmin(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/admins")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberAttributeService, times(1))
                .isAdmin(CURRENT_USER);
    }

    @Test
    public void getNumberOfGroupingsTest() throws Exception {
        final String uid = "grouping";
        final String owner = "bobo";

        String path = "grouping";

        List<GroupingPath> groupingPathList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            groupingPathList.add(new GroupingPath(path));
        }
        given(memberAttributeService.numberOfGroupings(owner, uid)).willReturn(10);

        mockMvc.perform(get(API_BASE + "/owners/" + uid + "/groupings/count")
                        .header(CURRENT_USER, owner))
                .andExpect(status().isOk());
        verify(memberAttributeService, times(1))
                .numberOfGroupings(owner, uid);
    }

    @Test
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
    public void syncDestinationsTest() throws Exception {
        MvcResult result =
                mockMvc.perform(get(API_BASE + "/groupings/" + groupingTwo().getPath() + "/sync-destinations")
                                .header("current_user", "o6-username"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(result, notNullValue());
    }

    @Test
    public void getNumberOfMembershipsTest() throws Exception {
        String uid = "uid";
        given(membershipService.numberOfMemberships(ADMIN, uid))
                .willReturn(369);

        mockMvc.perform(get(API_BASE + "/members/" + uid + "/memberships/count")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("369"));

        verify(membershipService, times(1))
                .numberOfMemberships(ADMIN, uid);
    }

    @Test
    public void isSoleOwnerTest() throws Exception {
        String uid = "uid";
        String path = "grouping-path";
        given(groupingAssignmentService.isSoleOwner(ADMIN, path, uid)).willReturn(true);
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/" + path + "/owners/" + uid)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).isSoleOwner(ADMIN, path, uid);
    }

    @Test
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
