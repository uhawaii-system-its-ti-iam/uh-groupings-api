package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.service.AsyncJobsManager;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingOwnerService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.Subject;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

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
    private AsyncJobsManager asyncJobsManager;

    @MockBean
    private GroupingAttributeService groupingAttributeService;

    @MockBean
    private GroupingAssignmentService groupingAssignmentService;

    @MockBean
    private MemberAttributeService memberAttributeService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private UpdateMemberService updateMemberService;
    @MockBean
    private GroupingOwnerService groupingOwnerService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "path:to:grouping";
    private static final String UID = "user";
    private static final String ADMIN = "admin";
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    // Test data.
    private Grouping grouping() {
        Grouping grouping = new Grouping("test:ing:me:bob");

        Group basisGroup = new Group();
        Subject subjectBasis0 = new Subject("b0-uid", "b0-name","b0-uuid");
        basisGroup.addMember(subjectBasis0);
        Subject subjectBasis1 = new Subject("b1-uid", "b1-name", "b1-uuid");
        basisGroup.addMember(subjectBasis1);
        Subject subjectBasis2 = new Subject("b2-uid", "b2-name", "b2-uuid");
        basisGroup.addMember(subjectBasis2);

        Group exclude = new Group();
        Subject subjectExclude0 = new Subject("e0-uid", "e0-name", "e0-uuid");
        exclude.addMember(subjectExclude0);
        grouping.setExclude(exclude);

        Group include = new Group();
        Subject subjectInclude0 = new Subject("i0-uid", "i0-name", "i0-uuid");
        include.addMember(subjectInclude0);
        Subject subjectInclude1 = new Subject("i1-uid", "i1-name", "i1-uuid");
        include.addMember(subjectInclude1);
        grouping.setInclude(include);

        Group owners = new Group();
        Subject subjectOwners0 = new Subject("o0-uid", "o0-name", "o0-uuid");
        owners.addMember(subjectOwners0);
        Subject subjectOwners1 = new Subject("o1-uid", "o1-name", "o1-uuid");
        owners.addMember(subjectOwners1);
        Subject subjectOwners2 = new Subject("o2-uid", "o2-name", "o2-uuid");
        owners.addMember(subjectOwners2);
        Subject subjectOwners3 = new Subject("o3-uid", "o3-name", "o3-uuid");
        owners.addMember(subjectOwners3);
        grouping.setOwners(owners);

        return grouping;
    }

    // Test data.
    private Grouping groupingTwo() {
        Grouping grouping = new Grouping("test:ing:me:kim");

        Group basisGroup = new Group();
        Subject subjectBasis0 = new Subject("b0-uid", "b0-name", "b0-uuid");
        basisGroup.addMember(subjectBasis0);
        Subject subjectBasis1 = new Subject("b1-uid", "b1-name", "b1-uuid");
        basisGroup.addMember(subjectBasis1);
        Subject subjectBasis2 = new Subject("b2-uid", "b2-name", "b2-uuid");
        basisGroup.addMember(subjectBasis2);
        grouping.setBasis(basisGroup);

        Group exclude = new Group();
        Subject subjectExclude0 = new Subject("e0-uid", "e0-name", "e0-uuid");
        exclude.addMember(subjectExclude0);
        grouping.setExclude(exclude);

        Group include = new Group();
        Subject subjectInclude0 = new Subject("i0-uid", "i0-name", "i0-uuid");
        include.addMember(subjectInclude0);
        Subject subjectInclude1 = new Subject("i1-uid", "i1-name", "i1-uuid");
        include.addMember(subjectInclude1);
        grouping.setInclude(include);

        Group owners = new Group();
        Subject subjectOwners0 = new Subject("o0-uid", "o0-name", "o0-uuid");
        owners.addMember(subjectOwners0);
        Subject subjectOwners1 = new Subject("o1-uid", "o1-name", "o1-uuid");
        owners.addMember(subjectOwners1);
        Subject subjectOwners2 = new Subject("o2-uid", "o2-name", "o2-uuid");
        owners.addMember(subjectOwners2);
        Subject subjectOwners3 = new Subject("o3-uid", "o3-name", "o3-uuid");
        owners.addMember(subjectOwners3);
        grouping.setOwners(owners);

        return grouping;
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
    public void groupingPathIsValidTest() throws Exception {
        given(groupingAttributeService.isGroupingPath(GROUPING)).willReturn(true);
        MvcResult result = mockMvc.perform(get(API_BASE + "/grouping/" + GROUPING +"/is-valid"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), is("true"));
        verify(groupingAttributeService, times(1)).isGroupingPath(GROUPING);
    }

    @Test
    public void groupingPathsTest() throws Exception {
        List<GroupingPath> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add(new GroupingPath("path:to:grouping" + i));
        }
        GroupingPaths groupingPaths = new GroupingPaths();
        groupingPaths.setGroupingPaths(paths);
        given(groupingAssignmentService.allGroupingPaths("bobo")).willReturn(groupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings")
                        .header(CURRENT_USER, "bobo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("groupingPaths[0].name").value("grouping0"))
                .andExpect(jsonPath("groupingPaths[1].name").value("grouping1"))
                .andExpect(jsonPath("groupingPaths[2].name").value("grouping2"))
                .andExpect(jsonPath("groupingPaths[0].path").value("path:to:grouping0"))
                .andExpect(jsonPath("groupingPaths[1].path").value("path:to:grouping1"))
                .andExpect(jsonPath("groupingPaths[2].path").value("path:to:grouping2"));
        verify(groupingAssignmentService, times(1))
                .allGroupingPaths("bobo");
    }

    @Test
    public void groupingAdminsTest() throws Exception {
        given(groupingAssignmentService.groupingAdmins("bobo")).willReturn(new GroupingGroupMembers());
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/admins")
                        .header(CURRENT_USER, "bobo"))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).groupingAdmins("bobo");
    }

    @Test
    public void addAdminTest() throws Exception {
        String adminToAdd = "adminToAdd";
        GroupingAddResult addMemberResult = new GroupingAddResult();
        given(updateMemberService.addAdminMember(ADMIN, adminToAdd)).willReturn(addMemberResult);
        mockMvc.perform(post(API_BASE + "/admins/" + adminToAdd)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());
        verify(updateMemberService, times(1))
                .addAdminMember(ADMIN, adminToAdd);
    }

    @Test
    public void removeAdminTest() throws Exception {
        String adminToRemove = "adminToRemove";
        GroupingRemoveResult removeMemberResult = new GroupingRemoveResult();
        given(updateMemberService.removeAdminMember(ADMIN, adminToRemove))
                .willReturn(removeMemberResult);

        mockMvc.perform(delete(API_BASE + "/admins/" + adminToRemove)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeAdminMember(ADMIN, adminToRemove);
    }

    @Test
    public void removeFromGroupsTest() throws Exception {
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add("grouping" + i);
        }
        String userToRemove = "userToRemove";
        given(updateMemberService.removeFromGroups(ADMIN, userToRemove, paths)).willReturn(groupingRemoveResults);
        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/" + String.join(",", paths) + "/" + userToRemove)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1)).removeFromGroups(ADMIN, userToRemove, paths);

    }

    @Test
    public void resetIncludeGroupTest() throws Exception {
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult();
        given(updateMemberService.resetIncludeGroup(ADMIN, "grouping")).willReturn(result);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/grouping/include")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetIncludeGroup(ADMIN, "grouping");
    }

    @Test
    public void resetIncludeGroupAsyncTest() throws Exception {
        CompletableFuture<GroupingReplaceGroupMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.resetIncludeGroupAsync(ADMIN, "grouping"))
                .willReturn(completableFuture);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/grouping/include/async")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetIncludeGroupAsync(ADMIN, "grouping");
    }

    @Test
    public void resetExcludeGroupTest() throws Exception {
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult();
        given(updateMemberService.resetExcludeGroup(ADMIN, "grouping")).willReturn(result);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetExcludeGroup(ADMIN, "grouping");
    }

    @Test
    public void resetExcludeGroupAsyncTest() throws Exception {
        CompletableFuture<GroupingReplaceGroupMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.resetExcludeGroupAsync(ADMIN, "grouping"))
                .willReturn(completableFuture);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude/async")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetExcludeGroupAsync(ADMIN, "grouping");
    }

    @Test
    public void memberAttributeResultsTest() throws Exception {
        List<String> members = new ArrayList<>();
        members.add("testiwta");
        members.add("testiwtb");
        MvcResult validResult = mockMvc.perform(post(API_BASE + "/members")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(validResult, notNullValue());

        verify(memberAttributeService, times(1))
                .getMemberAttributeResults(UID, members);
    }

    @Test
    public void memberAttributeResultsAsyncTest() throws Exception {
        List<String> members = new ArrayList<>();
        members.add("testiwta");
        members.add("testiwtb");
        CompletableFuture<MemberAttributeResults> completableFuture = new CompletableFuture<>();
        given(memberAttributeService.getMemberAttributeResultsAsync(UID, members))
                .willReturn(completableFuture);
        MvcResult validResult = mockMvc.perform(post(API_BASE + "/members/async")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertThat(validResult, notNullValue());

        verify(memberAttributeService, times(1))
                .getMemberAttributeResultsAsync(UID, members);
    }

    @Test
    public void ownedGroupingTest() throws Exception {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        assertNotNull(groupingGroupsMembers);
        List<String> paths =
                Arrays.asList("group-path:basis", "group-path:include", "group-path:exclude", "group-path:owners");
        given(groupingOwnerService.paginatedGrouping(CURRENT_USER, paths, 1, 700, "name", true))
                .willReturn(groupingGroupsMembers);
        MvcResult result = mockMvc.perform(
                        post(API_BASE + "/groupings/group?page=1&size=700&sortString=name&isAscending=true")
                                .header(CURRENT_USER, CURRENT_USER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.asJson(paths)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        assertEquals(JsonUtil.asJson(groupingGroupsMembers), result.getResponse().getContentAsString());
        verify(groupingOwnerService, times(1))
                .paginatedGrouping(CURRENT_USER, paths, 1, 700, "name", true);
    }

    @Test
    public void membershipResultsTest() throws Exception {
        MembershipResults memberships = new MembershipResults();
        given(membershipService.membershipResults(ADMIN, "testiwta")).willReturn(memberships);

        mockMvc.perform(get(API_BASE + "/members/testiwta/memberships")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .membershipResults(ADMIN, "testiwta");
    }

    @Test
    public void manageSubjectResultsTest() throws Exception {
        ManageSubjectResults manageSubjectResults = new ManageSubjectResults();
        given(membershipService.manageSubjectResults(ADMIN, "testiwta")).willReturn(manageSubjectResults);

        mockMvc.perform(get(API_BASE + "/members/testiwta/groupings")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .manageSubjectResults(ADMIN, "testiwta");
    }

    @Test
    public void getOptInGroupingPathsTest() throws Exception {
        GroupingPaths optInGroupingPaths = new GroupingPaths();
        given(groupingAssignmentService.optInGroupingPaths(ADMIN, "testiwta")).willReturn(optInGroupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings/members/testiwta/opt-in-groups")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk());

        verify(groupingAssignmentService, times(1))
                .optInGroupingPaths(ADMIN, "testiwta");
    }

    @Test
    public void optInTest() throws Exception {
        MvcResult includeResult =
                mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/include-members/o6-uid/self")
                                .header("current_user", "o6-uid")
                                .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(includeResult, notNullValue());
    }

    @Test
    public void optOutTest() throws Exception {
        MvcResult excludeResult =
                mockMvc.perform(put(API_BASE + "/groupings/test:ing:me:kim/exclude-members/o6-uid/self")
                                .header("current_user", "o6-uid")
                                .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(excludeResult, notNullValue());
    }

    @Test
    public void addIncludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        GroupingMoveMembersResult groupingMoveMembersResult = new GroupingMoveMembersResult();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(updateMemberService.addIncludeMembers(UID, "grouping", usersToAdd))
                .willReturn(groupingMoveMembersResult);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .addIncludeMembers(UID, "grouping", usersToAdd);
    }

    @Test
    public void addIncludeMembersAsyncTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        CompletableFuture<GroupingMoveMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.addIncludeMembersAsync(UID, "grouping", usersToAdd))
                .willReturn(completableFuture);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members/async")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isAccepted());

        verify(updateMemberService, times(1))
                .addIncludeMembersAsync(UID, "grouping", usersToAdd);
    }

    @Test
    public void addExcludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        GroupingMoveMembersResult groupingMoveMembersResult = new GroupingMoveMembersResult();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(updateMemberService.addExcludeMembers(UID, "grouping", usersToAdd))
                .willReturn(groupingMoveMembersResult);

        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .addExcludeMembers(UID, "grouping", usersToAdd);
    }

    @Test
    public void addExcludeMembersAsyncTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        CompletableFuture<GroupingMoveMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.addExcludeMembersAsync(UID, "grouping", usersToAdd))
                .willReturn(completableFuture);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members/async")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isAccepted());

        verify(updateMemberService, times(1))
                .addExcludeMembersAsync(UID, "grouping", usersToAdd);
    }

    @Test
    public void removeIncludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(updateMemberService.removeIncludeMembers(UID, "grouping", usersToRemove))
                .willReturn(groupingRemoveResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/include-members")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeIncludeMembers(UID, "grouping", usersToRemove);
    }

    @Test
    public void removeExcludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(updateMemberService.removeExcludeMembers(UID, "grouping", usersToRemove))
                .willReturn(groupingRemoveResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude-members")
                        .header(CURRENT_USER, UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeExcludeMembers(UID, "grouping", usersToRemove);
    }

    @Test
    public void ownerGroupingsTest() throws Exception {
        final String uid = "grouping";
        final String admin = "bobo";

        String path = "path:to:grouping";
        String description = "description";

        GroupingPaths groupingPaths = new GroupingPaths();
        groupingPaths.addGroupingPath(new GroupingPath(path, description));

        given(memberAttributeService.getOwnedGroupings(admin, uid))
                .willReturn(groupingPaths);
        mockMvc.perform(get(API_BASE + "/owners/grouping/groupings")
                        .header(CURRENT_USER, admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.groupingPaths[0].path").value(path))
                .andExpect(jsonPath("$.groupingPaths[0].name").value("grouping"))
                .andExpect(jsonPath("$.groupingPaths[0].description").value("description"));

        verify(memberAttributeService, times(1))
                .getOwnedGroupings(admin, uid);
    }

    @Test
    public void addOwnersTest() throws Exception {
        List<String> ownersToAdd = new ArrayList<>();
        GroupingAddResults groupingAddResults = new GroupingAddResults();
        ownersToAdd.add("tst04name");
        ownersToAdd.add("tst05name");
        ownersToAdd.add("tst06name");

        given(updateMemberService.addOwnerships(UID, "grouping", ownersToAdd))
                .willReturn(groupingAddResults);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToAdd))
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(updateMemberService, times(1))
                .addOwnerships(UID, "grouping", ownersToAdd);

    }

    @Test
    public void addGroupPathOwnersTest() throws Exception {
        List<String> pathOwnersToAdd = new ArrayList<>();
        GroupingAddResults groupingAddResults = new GroupingAddResults();
        pathOwnersToAdd.add("tmp:tst04name:groupPath04");
        pathOwnersToAdd.add("tmp:tst05name:groupPath05");
        pathOwnersToAdd.add("tmp:tst06name:groupPath06");

        given(updateMemberService.addGroupPathOwnership(UID, "grouping", pathOwnersToAdd))
                .willReturn(groupingAddResults);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/path-owner/" + String.join(",", pathOwnersToAdd))
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(updateMemberService, times(1))
                .addGroupPathOwnership(UID, "grouping", pathOwnersToAdd);
    }

    @Test
    public void removeOwnersTest() throws Exception {
        List<String> ownersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        ownersToRemove.add("tst04name");
        ownersToRemove.add("tst05name");
        ownersToRemove.add("tst06name");

        given(updateMemberService.removeOwnerships(UID, "grouping", ownersToRemove))
                .willReturn(groupingRemoveResults);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToRemove))
                                .header(CURRENT_USER, UID))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1))
                .removeOwnerships(UID, "grouping", ownersToRemove);
    }

    @Test
    public void removeGroupPathOwnersTest() throws Exception {
        List<String> pathOwnersToAdd = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        pathOwnersToAdd.add("tmp:tst04name:groupPath04");
        pathOwnersToAdd.add("tmp:tst05name:groupPath05");
        pathOwnersToAdd.add("tmp:tst06name:groupPath06");

        given(updateMemberService.removeOwnerships(UID, "grouping", pathOwnersToAdd))
                .willReturn(groupingRemoveResults);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/path-owner/" + String.join(",", pathOwnersToAdd))
                                .header(CURRENT_USER, UID))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1))
                .removeGroupPathOwnerships(UID, "grouping", pathOwnersToAdd);
    }

    @Test
    public void updateDescriptionTest() throws Exception {
        GroupingUpdateDescriptionResult groupingsUpdateDescriptionResult = new GroupingUpdateDescriptionResult();

        given(groupingAttributeService.updateDescription("grouping", UID, "description")).willReturn(
                groupingsUpdateDescriptionResult);
        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/description")
                        .header(CURRENT_USER, UID)
                        .content("description"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);

        verify(groupingAttributeService, times(1))
                .updateDescription("grouping", UID, "description");
    }

    @Test
    public void enablePreferenceSyncDestTest() throws Exception {

        OptRequest optInRequest = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withUid(UID)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptValue(true)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withOptType(OptType.IN)
                .withUid(UID)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptValue(true)
                .build();

        given(groupingAttributeService.changeOptStatus(optInRequest, optOutRequest)).willReturn(gsrListIn());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preference/" + OptType.IN.value() + "/enable")
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-in"));
        verify(groupingAttributeService, times(1)).changeOptStatus(optInRequest, optOutRequest);

        optInRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withUid(UID)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.IN)
                .withOptValue(false)
                .build();

        optOutRequest = new OptRequest.Builder()
                .withOptType(OptType.OUT)
                .withUid(UID)
                .withGroupNameRoot("grouping")
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptValue(false)
                .build();

        given(groupingAttributeService.changeOptStatus(optInRequest, optOutRequest)).willReturn(gsrListOut());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/preference/" + OptType.OUT.value() + "/disable")
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resultCode").value(SUCCESS))
                .andExpect(jsonPath("$[0].action").value("member is opted-out"));
        verify(groupingAttributeService, times(1)).changeOptStatus(optInRequest, optOutRequest);

        given(groupingAttributeService.changeGroupAttributeStatus("grouping", UID, LISTSERV, true))
                .willReturn(gsrListserv());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + LISTSERV + "/enable")
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("listserv status changed"));

        verify(groupingAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", UID, LISTSERV, true);

        given(groupingAttributeService.changeGroupAttributeStatus("grouping", UID, RELEASED_GROUPING, true))
                .willReturn(gsrReleasedGrouping());
        mockMvc.perform(put(API_BASE + "/groupings/grouping/sync-destination/" + RELEASED_GROUPING + "/enable")
                        .header(CURRENT_USER, UID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(SUCCESS))
                .andExpect(jsonPath("$.action").value("ldap status changed"));

        verify(groupingAttributeService, times(1))
                .changeGroupAttributeStatus("grouping", UID, RELEASED_GROUPING, true);
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        given(memberService.isOwner(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/owners")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberService, times(1))
                .isOwner(CURRENT_USER);
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        given(memberService.isAdmin(CURRENT_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/members/is-admin")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberService, times(1))
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
    public void getGroupingDescriptionTest() throws Exception {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        GroupingDescription groupingDescription = new GroupingDescription(findGroupsResults.getGroup());
        given(groupingOwnerService.groupingsDescription(CURRENT_USER, "grouping-path")).willReturn(
                groupingDescription);
        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/grouping-path/description")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        assertEquals(JsonUtil.asJson(groupingDescription), result.getResponse().getContentAsString());
        verify(groupingOwnerService, times(1))
                .groupingsDescription(CURRENT_USER, "grouping-path");
    }

    @Test
    public void groupingOptAttributesTest() throws Exception {
        String json = propertyLocator.find("ws.get.attribute.assignment.results.optIn-on.optOut-on");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertNotNull(groupingOptAttributes);
        String groupingPath = "grouping-path";
        given(groupingOwnerService.groupingOptAttributes(CURRENT_USER, groupingPath))
                .willReturn(groupingOptAttributes);
        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/" + groupingPath + "/opt-attributes")
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        assertEquals(JsonUtil.asJson(groupingOptAttributes), result.getResponse().getContentAsString());
        verify(groupingOwnerService, times(1))
                .groupingOptAttributes(CURRENT_USER, groupingPath);
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
    public void groupingOwnersTest() throws Exception {
        String path = "grouping-path";
        given(groupingAssignmentService.groupingOwners(ADMIN, path)).willReturn(new GroupingGroupMembers());
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/grouping/" + path + "/owners")
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).groupingOwners(ADMIN, path);
    }

    @Test
    public void getAsyncJobResultTest() throws Exception {
        CompletableFuture<?> completableFuture = new CompletableFuture<>();
        Integer jobId = completableFuture.hashCode();
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/jobs/" + jobId)
                        .header(CURRENT_USER, CURRENT_USER))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(asyncJobsManager, times(1)).getJobResult(CURRENT_USER, jobId);
    }


    @Test
    public void regexTest() throws Exception {
        // Sending an 'unsafe character' in the URI should get rejected and return SERVER_ERROR
        MvcResult result1 = mockMvc.perform(get(API_BASE + "/owners/" + UID + "[" + "/groupings")
                        .header(CURRENT_USER, UID))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result1, notNullValue());

        MvcResult result2 = mockMvc.perform(get(API_BASE + "/owners/" + UID + "^" + "/groupings")
                        .header(CURRENT_USER, UID))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result2, notNullValue());

        MvcResult result3 = mockMvc.perform(get(API_BASE + "/members/" + UID + "}")
                        .header(CURRENT_USER, UID))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result3, notNullValue());

        MvcResult result4 = mockMvc.perform(get(API_BASE + "/members/" + UID + "@")
                        .header(CURRENT_USER, UID))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result4, notNullValue());
    }
}