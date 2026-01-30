package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
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

import edu.hawaii.its.api.configuration.SecurityTestConfig;
import edu.hawaii.its.api.type.SortBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingOwnerMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdateSyncDestResult;
import edu.hawaii.its.api.groupings.GroupingUpdatedAttributeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateOptAttributeResult;

import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.service.AsyncJobsManager;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingOwnerService;
import edu.hawaii.its.api.service.GroupingsService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.Subject;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
@Import(SecurityTestConfig.class)
public class GroupingsRestControllerv2_1Test {

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.max.owner.limit}")
    private Integer OWNER_LIMIT;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.localhost.user}")
    private String TEST_USER;

    @MockitoBean
    private AsyncJobsManager asyncJobsManager;

    @MockitoBean
    private GroupingAttributeService groupingAttributeService;

    @MockitoBean
    private GroupingAssignmentService groupingAssignmentService;

    @MockitoBean
    private MemberAttributeService memberAttributeService;

    @MockitoBean
    private MembershipService membershipService;

    @MockitoBean
    private UpdateMemberService updateMemberService;
    @MockitoBean
    private GroupingOwnerService groupingOwnerService;

    @MockitoBean
    private GroupingsService groupingsService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "path:to:grouping";
    private PropertyLocator propertyLocator;

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    // Test data.
    private Grouping grouping() {
        Grouping grouping = new Grouping("tmp:tst01name:groupPath01");

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
        Grouping grouping = new Grouping("tmp:tst02name:groupPath02");

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
    @WithMockUhAdmin
    public void groupingPathsTest() throws Exception {
        List<GroupingPath> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add(new GroupingPath(GROUPING + i));
        }
        GroupingPaths groupingPaths = new GroupingPaths();
        groupingPaths.setGroupingPaths(paths);
        given(groupingAssignmentService.allGroupingPaths(ADMIN)).willReturn(groupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("groupingPaths[0].name").value("grouping0"))
                .andExpect(jsonPath("groupingPaths[1].name").value("grouping1"))
                .andExpect(jsonPath("groupingPaths[2].name").value("grouping2"))
                .andExpect(jsonPath("groupingPaths[0].path").value("path:to:grouping0"))
                .andExpect(jsonPath("groupingPaths[1].path").value("path:to:grouping1"))
                .andExpect(jsonPath("groupingPaths[2].path").value("path:to:grouping2"));
        verify(groupingAssignmentService, times(1))
                .allGroupingPaths(ADMIN);
    }

    @Test
    @WithMockUhAdmin
    public void groupingAdminsTest() throws Exception {
        given(groupingAssignmentService.groupingAdmins(ADMIN)).willReturn(new GroupingGroupMembers());
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/admins"))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).groupingAdmins(ADMIN);
    }

    @Test
    @WithMockUhAdmin
    public void addAdminTest() throws Exception {
        String adminToAdd = "testiwta";
        GroupingAddResult addMemberResult = new GroupingAddResult();
        given(updateMemberService.addAdminMember(ADMIN, adminToAdd)).willReturn(addMemberResult);
        mockMvc.perform(post(API_BASE + "/admins/" + adminToAdd))
                .andExpect(status().isOk());
        verify(updateMemberService, times(1))
                .addAdminMember(ADMIN, adminToAdd);
    }

    @Test
    @WithMockUhAdmin
    public void removeAdminTest() throws Exception {
        String adminToRemove = "testiwta";
        GroupingRemoveResult removeMemberResult = new GroupingRemoveResult();
        given(updateMemberService.removeAdminMember(ADMIN, adminToRemove))
                .willReturn(removeMemberResult);

        mockMvc.perform(delete(API_BASE + "/admins/" + adminToRemove))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeAdminMember(ADMIN, adminToRemove);
    }

    @Test
    @WithMockUhAdmin
    public void removeFromGroupsTest() throws Exception {
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add(GROUPING + i);
        }
        String userToRemove = "testiwta";
        given(updateMemberService.removeFromGroups(ADMIN, userToRemove, paths)).willReturn(groupingRemoveResults);
        MvcResult result = mockMvc.perform(delete(API_BASE + "/admins/" + String.join(",", paths) + "/" + userToRemove))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1)).removeFromGroups(ADMIN, userToRemove, paths);

    }

    @Test
    @WithMockUhOwner
    public void resetIncludeGroupTest() throws Exception {
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult();
        given(updateMemberService.resetIncludeGroup(TEST_USER, "grouping")).willReturn(result);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/grouping/include"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetIncludeGroup(TEST_USER, "grouping");
    }

    @Test
    @WithMockUhAdmin
    public void resetIncludeGroupAsyncTest() throws Exception {
        CompletableFuture<GroupingReplaceGroupMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.resetIncludeGroupAsync(ADMIN, GROUPING))
                .willReturn(completableFuture);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/" + GROUPING + "/include/async"))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetIncludeGroupAsync(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhOwner
    public void resetExcludeGroupTest() throws Exception {
        GroupingReplaceGroupMembersResult result = new GroupingReplaceGroupMembersResult();
        given(updateMemberService.resetExcludeGroup(TEST_USER, "grouping")).willReturn(result);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/" + GROUPING + "/exclude"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetExcludeGroup(TEST_USER, GROUPING);
    }

    @Test
    @WithMockUhAdmin
    public void resetExcludeGroupAsyncTest() throws Exception {
        CompletableFuture<GroupingReplaceGroupMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.resetExcludeGroupAsync(ADMIN, GROUPING))
                .willReturn(completableFuture);

        MvcResult mvcResult = mockMvc.perform(delete(API_BASE + "/groupings/"  + GROUPING + "/exclude/async"))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(mvcResult);

        verify(updateMemberService, times(1)).resetExcludeGroupAsync(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhOwner
    public void memberAttributeResultsTest() throws Exception {
        List<String> members = new ArrayList<>();
        members.add("testiwta");
        members.add("testiwtb");
        MvcResult validResult = mockMvc.perform(post(API_BASE + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(validResult, notNullValue());

        verify(memberAttributeService, times(1))
                .getMemberAttributeResults(TEST_USER, members);
    }

    @Test
    @WithMockUhOwner
    public void memberAttributeResultsAsyncTest() throws Exception {
        List<String> members = new ArrayList<>();
        members.add("testiwta");
        members.add("testiwtb");
        CompletableFuture<MemberAttributeResults> completableFuture = new CompletableFuture<>();
        given(memberAttributeService.getMemberAttributeResultsAsync(TEST_USER, members))
                .willReturn(completableFuture);
        MvcResult validResult = mockMvc.perform(post(API_BASE + "/members/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertThat(validResult, notNullValue());

        verify(memberAttributeService, times(1))
                .getMemberAttributeResultsAsync(TEST_USER, members);
    }

    @Test
    @WithMockUhOwner
    public void ownedGroupingTest() throws Exception {
        SortBy[] sortByOptions = { SortBy.NAME, SortBy.UID, SortBy.UH_UUID };
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        List<String> paths = Arrays.asList("group-path:basis", "group-path:include", "group-path:exclude", "group-path:owners");
        for (SortBy sortBy : sortByOptions) {
            given(groupingOwnerService.paginatedGrouping(TEST_USER, paths, 1, 700, sortBy.sortString(), true))
                .willReturn(groupingGroupsMembers);
            MvcResult result = mockMvc.perform(
                post(API_BASE + "/groupings/group?page=1&size=700&sortBy=" + sortBy.value() + "&isAscending=true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JsonUtil.asJson(paths)))
                    .andExpect(status().isOk())
                    .andReturn();
            assertEquals(JsonUtil.asJson(groupingGroupsMembers), result.getResponse().getContentAsString());
            verify(groupingOwnerService).paginatedGrouping(TEST_USER, paths, 1, 700, sortBy.sortString(), true);
        }
    }

    @Test
    @WithMockUhOwner
    public void getGroupingMembersTest() throws Exception {
        SortBy[] sortByOptions = { SortBy.NAME, SortBy.UID, SortBy.UH_UUID };
        String json = propertyLocator.find("ws.get.members.results.success");
        WsGetMembersResult wsGetMembersResult = JsonUtil.asObject(json, WsGetMembersResult.class);
        GetMembersResult getMembersResult = new GetMembersResult(wsGetMembersResult);
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResult);
        assertNotNull(groupingGroupMembers);
        String path = "group-path:include";
        for (SortBy sortBy : sortByOptions) {
            given(groupingOwnerService.getGroupingMembers(TEST_USER, path, 1, 700,
                    sortBy.sortString(), true, "test")).willReturn(groupingGroupMembers);
            MvcResult result = mockMvc.perform(
                            get(API_BASE + "/groupings/" + path + "?page=1&size=700&sortBy=" + sortBy.value()
                                    + "&isAscending=true&searchString=test"))
                    .andExpect(status().isOk())
                    .andReturn();
            assertNotNull(result);
            assertEquals(JsonUtil.asJson(groupingGroupMembers), result.getResponse().getContentAsString());
            verify(groupingOwnerService, times(1)).getGroupingMembers(TEST_USER, path,
                    1, 700, sortBy.sortString(), true, "test");
        }

    }

    @Test
    @WithMockUhAdmin
    public void getGroupingMembersWhereListedTest() throws Exception {
        String path = "path:to:grouping";
        List<String> members = Arrays.asList("testiwta", "testiwtb");
        MvcResult mvcResult = mockMvc.perform(post(API_BASE + "/groupings/" + path + "/where-listed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingOwnerService, times(1)).getGroupingMembersWhereListed(ADMIN, path, members);
    }

    @Test
    @WithMockUhAdmin
    public void getGroupingMembersIsBasisTest() throws Exception {
        String path = "path:to:grouping";
        List<String> members = Arrays.asList("testiwta", "testiwtb");
        MvcResult mvcResult = mockMvc.perform(post(API_BASE + "/groupings/" + path + "/is-basis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingOwnerService, times(1)).getGroupingMembersIsBasis(ADMIN, path, members);
    }

    @Test
    @WithMockUhOwner
    public void membershipResultsTest() throws Exception {
        MembershipResults memberships = new MembershipResults();
        given(membershipService.membershipResults(TEST_USER)).willReturn(memberships);

        mockMvc.perform(get(API_BASE + "/members/memberships"))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .membershipResults(TEST_USER);
    }

    @Test
    @WithMockUhAdmin
    public void manageSubjectResultsTest() throws Exception {
        ManageSubjectResults manageSubjectResults = new ManageSubjectResults();
        given(membershipService.manageSubjectResults(ADMIN, "testiwta")).willReturn(manageSubjectResults);

        mockMvc.perform(get(API_BASE + "/members/testiwta/groupings"))
                .andExpect(status().isOk());

        verify(membershipService, times(1))
                .manageSubjectResults(ADMIN, "testiwta");
    }

    @Test
    @WithMockUhAdmin
    public void getOptInGroupingPathsTest() throws Exception {
        GroupingPaths optInGroupingPaths = new GroupingPaths();
        given(groupingAssignmentService.optInGroupingPaths(ADMIN, "testiwta")).willReturn(optInGroupingPaths);
        mockMvc.perform(get(API_BASE + "/groupings/members/testiwta/opt-in-groups"))
                .andExpect(status().isOk());

        verify(groupingAssignmentService, times(1))
                .optInGroupingPaths(ADMIN, "testiwta");
    }

    @Test
    @WithMockUhOwner
    public void optInTest() throws Exception {
        MvcResult includeResult =
                mockMvc.perform(put(API_BASE + "/groupings/tmp:tst02name:groupPath02/include-members/o6-uid/self")
                                .header("current_user", "o6-uid")
                                .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(includeResult, notNullValue());
    }

    @Test
    @WithMockUhOwner
    public void optOutTest() throws Exception {
        MvcResult excludeResult =
                mockMvc.perform(put(API_BASE + "/groupings/tmp:tst02name:groupPath02/exclude-members/o6-uid/self")
                                .header("accept", "application/json"))
                        .andExpect(status().isOk())
                        .andReturn();
        assertThat(excludeResult, notNullValue());
    }

    @Test
    @WithMockUhOwner
    public void addIncludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        GroupingMoveMembersResult groupingMoveMembersResult = new GroupingMoveMembersResult();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(updateMemberService.addIncludeMembers(TEST_USER, "grouping", usersToAdd))
                .willReturn(groupingMoveMembersResult);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .addIncludeMembers(TEST_USER, "grouping", usersToAdd);
    }

    @Test
    @WithMockUhOwner
    public void addIncludeMembersAsyncTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        CompletableFuture<GroupingMoveMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.addIncludeMembersAsync(TEST_USER, "grouping", usersToAdd))
                .willReturn(completableFuture);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/include-members/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isAccepted());

        verify(updateMemberService, times(1))
                .addIncludeMembersAsync(TEST_USER, "grouping", usersToAdd);
    }

    @Test
    @WithMockUhOwner
    public void addExcludeMembersTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        GroupingMoveMembersResult groupingMoveMembersResult = new GroupingMoveMembersResult();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        given(updateMemberService.addExcludeMembers(TEST_USER, "grouping", usersToAdd))
                .willReturn(groupingMoveMembersResult);

        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .addExcludeMembers(TEST_USER, "grouping", usersToAdd);
    }

    @Test
    @WithMockUhOwner
    public void addExcludeMembersAsyncTest() throws Exception {
        List<String> usersToAdd = new ArrayList<>();
        usersToAdd.add("tst04name");
        usersToAdd.add("tst05name");
        usersToAdd.add("tst06name");
        CompletableFuture<GroupingMoveMembersResult> completableFuture = new CompletableFuture<>();
        given(updateMemberService.addExcludeMembersAsync(TEST_USER, "grouping", usersToAdd))
                .willReturn(completableFuture);
        mockMvc.perform(put(API_BASE + "/groupings/grouping/exclude-members/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToAdd)))
                .andExpect(status().isAccepted());

        verify(updateMemberService, times(1))
                .addExcludeMembersAsync(TEST_USER, "grouping", usersToAdd);
    }

    @Test
    @WithMockUhOwner
    public void removeIncludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(updateMemberService.removeIncludeMembers(TEST_USER, "grouping", usersToRemove))
                .willReturn(groupingRemoveResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/include-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeIncludeMembers(TEST_USER, "grouping", usersToRemove);
    }

    @Test
    @WithMockUhOwner
    public void removeExcludeMembersTest() throws Exception {
        List<String> usersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        usersToRemove.add("tst04name");
        usersToRemove.add("tst05name");
        usersToRemove.add("tst06name");
        given(updateMemberService.removeExcludeMembers(TEST_USER, "grouping", usersToRemove))
                .willReturn(groupingRemoveResults);
        mockMvc.perform(delete(API_BASE + "/groupings/grouping/exclude-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(usersToRemove)))
                .andExpect(status().isOk());

        verify(updateMemberService, times(1))
                .removeExcludeMembers(TEST_USER, "grouping", usersToRemove);
    }

    @Test
    @WithMockUhAdmin
    public void ownerGroupingsTest() throws Exception {
        String path = "path:to:grouping";
        String description = "description";

        GroupingPaths groupingPaths = new GroupingPaths();
        groupingPaths.addGroupingPath(new GroupingPath(path, description));

        given(memberAttributeService.getOwnedGroupings(ADMIN))
                .willReturn(groupingPaths);
        mockMvc.perform(get(API_BASE + "/owners/groupings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.groupingPaths[0].path").value(path))
                .andExpect(jsonPath("$.groupingPaths[0].name").value("grouping"))
                .andExpect(jsonPath("$.groupingPaths[0].description").value("description"));

        verify(memberAttributeService, times(1))
                .getOwnedGroupings(ADMIN);
    }

    @Test
    @WithMockUhOwner
    public void addOwnersTest() throws Exception {
        List<String> ownersToAdd = new ArrayList<>();
        GroupingAddResults groupingAddResults = new GroupingAddResults();
        ownersToAdd.add("tst04name");
        ownersToAdd.add("tst05name");
        ownersToAdd.add("tst06name");

        given(updateMemberService.addOwnerships(TEST_USER, "grouping", ownersToAdd))
                .willReturn(groupingAddResults);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToAdd)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(updateMemberService, times(1))
                .addOwnerships(TEST_USER, "grouping", ownersToAdd);

    }

    @Test
    @WithMockUhOwner
    public void addOwnerGroupingsTest() throws Exception {
        List<String> ownerGroupingsToAdd = new ArrayList<>();
        GroupingAddResults groupingAddResults = new GroupingAddResults();
        ownerGroupingsToAdd.add("tmp:tst04name:groupPath04");
        ownerGroupingsToAdd.add("tmp:tst05name:groupPath05");
        ownerGroupingsToAdd.add("tmp:tst06name:groupPath06");

        given(updateMemberService.addOwnerGroupingOwnerships(TEST_USER, "grouping", ownerGroupingsToAdd))
                .willReturn(groupingAddResults);

        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/owners/owner-groupings/" + String.join(",", ownerGroupingsToAdd)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result, notNullValue());
        verify(updateMemberService, times(1))
                .addOwnerGroupingOwnerships(TEST_USER, "grouping", ownerGroupingsToAdd);
    }

    @Test
    @WithMockUhOwner
    public void removeOwnersTest() throws Exception {
        List<String> ownersToRemove = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        ownersToRemove.add("tst04name");
        ownersToRemove.add("tst05name");
        ownersToRemove.add("tst06name");

        given(updateMemberService.removeOwnerships(TEST_USER, "grouping", ownersToRemove))
                .willReturn(groupingRemoveResults);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/" + String.join(",", ownersToRemove)))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1))
                .removeOwnerships(TEST_USER, "grouping", ownersToRemove);
    }

    @Test
    @WithMockUhOwner
    public void removeOwnerGroupingsTest() throws Exception {
        List<String> ownerGroupingsToAdd = new ArrayList<>();
        GroupingRemoveResults groupingRemoveResults = new GroupingRemoveResults();
        ownerGroupingsToAdd.add("tmp:tst04name:groupPath04");
        ownerGroupingsToAdd.add("tmp:tst05name:groupPath05");
        ownerGroupingsToAdd.add("tmp:tst06name:groupPath06");

        given(updateMemberService.removeOwnerships(TEST_USER, "grouping", ownerGroupingsToAdd))
                .willReturn(groupingRemoveResults);

        MvcResult result =
                mockMvc.perform(delete(API_BASE + "/groupings/grouping/owners/owner-groupings/" + String.join(",", ownerGroupingsToAdd)))
                        .andExpect(status().isOk())
                        .andReturn();
        assertNotNull(result);
        verify(updateMemberService, times(1))
                .removeOwnerGroupingOwnerships(TEST_USER, "grouping", ownerGroupingsToAdd);
    }

    @Test
    @WithMockUhOwner
    public void updateDescriptionTest() throws Exception {
        GroupingUpdateDescriptionResult groupingsUpdateDescriptionResult = new GroupingUpdateDescriptionResult();

        given(groupingAttributeService.updateDescription("grouping", TEST_USER, "description")).willReturn(
                groupingsUpdateDescriptionResult);
        MvcResult result = mockMvc.perform(put(API_BASE + "/groupings/grouping/description")
                        .content("description"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);

        verify(groupingAttributeService, times(1))
                .updateDescription("grouping", TEST_USER, "description");
    }

    @Test
    @WithMockUhOwner
    public void updateSyncDestTest() throws Exception {

        given(groupingAttributeService.updateGroupingSyncDest(GROUPING, TEST_USER, LISTSERV, true))
                .willReturn(new GroupingUpdateSyncDestResult(new GroupingUpdatedAttributeResult()));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/sync-destination/" + LISTSERV + "/true"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateGroupingSyncDest(GROUPING, TEST_USER, LISTSERV, true);

        given(groupingAttributeService.updateGroupingSyncDest(GROUPING, TEST_USER, LISTSERV, false))
                .willReturn(new GroupingUpdateSyncDestResult(new GroupingUpdatedAttributeResult()));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/sync-destination/" + LISTSERV + "/false"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateGroupingSyncDest(GROUPING, TEST_USER, LISTSERV, false);

        given(groupingAttributeService.updateGroupingSyncDest(GROUPING, TEST_USER, RELEASED_GROUPING, true))
                .willReturn(new GroupingUpdateSyncDestResult(new GroupingUpdatedAttributeResult()));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/sync-destination/" + RELEASED_GROUPING + "/true"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateGroupingSyncDest(GROUPING, TEST_USER, RELEASED_GROUPING, true);

        given(groupingAttributeService.updateGroupingSyncDest(GROUPING, TEST_USER, RELEASED_GROUPING, false))
                .willReturn(new GroupingUpdateSyncDestResult(new GroupingUpdatedAttributeResult()));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/sync-destination/" + RELEASED_GROUPING + "/false"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateGroupingSyncDest(GROUPING, TEST_USER, RELEASED_GROUPING, false);
    }

    @Test
    @WithMockUhOwner
    public void updateOptAttributeTest() throws Exception {
        // case 1: id=IN, status=true
        given(groupingAttributeService.updateOptAttribute(any(OptRequest.class), any(OptRequest.class)))
                .willReturn(mock(GroupingUpdateOptAttributeResult.class));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/opt-attribute/" + OptType.IN.value() + "/true"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateOptAttribute(any(OptRequest.class), any(OptRequest.class));

        // case 2: id=IN, status=false
        reset(groupingAttributeService);
        given(groupingAttributeService.updateOptAttribute(any(OptRequest.class), any(OptRequest.class)))
                .willReturn(mock(GroupingUpdateOptAttributeResult.class));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/opt-attribute/" + OptType.IN.value() + "/false"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateOptAttribute(any(OptRequest.class), any(OptRequest.class));

        // case 3: id=OUT, status=true
        reset(groupingAttributeService);
        given(groupingAttributeService.updateOptAttribute(any(OptRequest.class), any(OptRequest.class)))
                .willReturn(mock(GroupingUpdateOptAttributeResult.class));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/opt-attribute/" + OptType.OUT.value() + "/true"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateOptAttribute(any(OptRequest.class), any(OptRequest.class));

        // case 4: id=OUT, status=false
        reset(groupingAttributeService);
        given(groupingAttributeService.updateOptAttribute(any(OptRequest.class), any(OptRequest.class)))
                .willReturn(mock(GroupingUpdateOptAttributeResult.class));

        mockMvc.perform(put(API_BASE + "/groupings/" + GROUPING + "/opt-attribute/" + OptType.OUT.value() + "/false"))
                .andExpect(status().isOk());

        verify(groupingAttributeService, times(1))
                .updateOptAttribute(any(OptRequest.class), any(OptRequest.class));
    }


    @Test
    public void hasOwnerPrivsTest() throws Exception {
        given(memberService.isOwner(TEST_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/members/" + TEST_USER + "/is-owner"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberService, times(1))
                .isOwner(TEST_USER);
    }

    @Test
    public void hasGroupingOwnerPrivsTest() throws Exception {
        String groupingPath = "grouping-path";
        given(memberService.isOwner(groupingPath, TEST_USER)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/members/" + groupingPath + "/" + TEST_USER + "/is-owner"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberService, times(1))
                .isOwner(groupingPath, TEST_USER);
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        given(memberService.isAdmin(ADMIN)).willReturn(false);
        MvcResult result = mockMvc.perform(get(API_BASE + "/members/" + ADMIN + "/is-admin"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(memberService, times(1))
                .isAdmin(ADMIN);
    }

    @Test
    @WithMockUhOwner
    public void getNumberOfGroupingsTest() throws Exception {

        List<GroupingPath> groupingPathList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            groupingPathList.add(new GroupingPath(GROUPING));
        }
        given(memberAttributeService.numberOfGroupings(TEST_USER)).willReturn(10);

        mockMvc.perform(get(API_BASE + "/owners/groupings/count"))
                .andExpect(status().isOk());
        verify(memberAttributeService, times(1))
                .numberOfGroupings(TEST_USER);
    }

    @Test
    @WithMockUhOwner
    public void getGroupingDescriptionTest() throws Exception {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        GroupingDescription groupingDescription = new GroupingDescription(findGroupsResults.getGroup());
        given(groupingOwnerService.groupingsDescription(TEST_USER, GROUPING)).willReturn(
                groupingDescription);
        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING + "/description"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        assertEquals(JsonUtil.asJson(groupingDescription), result.getResponse().getContentAsString());
        verify(groupingOwnerService, times(1))
                .groupingsDescription(TEST_USER, GROUPING);
    }

    @Test
    @WithMockUhOwner
    public void groupingOptAttributesTest() throws Exception {
        String json = propertyLocator.find("ws.get.attribute.assignment.results.optIn-on.optOut-on");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
        GroupingOptAttributes groupingOptAttributes = new GroupingOptAttributes(groupAttributeResults);
        assertNotNull(groupingOptAttributes);

        given(groupingOwnerService.groupingOptAttributes(TEST_USER, GROUPING))
                .willReturn(groupingOptAttributes);
        MvcResult result = mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING + "/opt-attributes"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        assertEquals(JsonUtil.asJson(groupingOptAttributes), result.getResponse().getContentAsString());
        verify(groupingOwnerService, times(1))
                .groupingOptAttributes(TEST_USER, GROUPING);
    }

    @Test
    @WithMockUhOwner
    public void getNumberOfMembershipsTest() throws Exception {

        given(membershipService.numberOfMemberships(TEST_USER))
                .willReturn(369);

        mockMvc.perform(get(API_BASE + "/members/memberships/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("369"));

        verify(membershipService, times(1))
                .numberOfMemberships(TEST_USER);
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfGroupingMembersTest() throws Exception {
        given(groupingOwnerService.numberOfGroupingMembers(ADMIN, GROUPING))
                .willReturn(100);

        mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING + "/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(groupingOwnerService, times(1))
                .numberOfGroupingMembers(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfOwnersTest() throws Exception {
        given(groupingAssignmentService.numberOfDirectOwners(ADMIN, GROUPING)).willReturn(1);
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/members/" + GROUPING + "/owners/count"))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).numberOfDirectOwners(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfAllOwnersTest() throws Exception {
        given(groupingAssignmentService.numberOfAllOwners(ADMIN, GROUPING)).willReturn(1);

        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING + "/owners/count"))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).numberOfAllOwners(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhAdmin
    public void compareOwnerGroupingsTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/groupings/" + GROUPING + "/owners/compare"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).compareOwnerGroupings(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhAdmin
    public void groupingOwnersTest() throws Exception {
        given(groupingAssignmentService.groupingImmediateOwners(ADMIN, GROUPING)).willReturn(new GroupingOwnerMembers(OWNER_LIMIT));
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/grouping/" + GROUPING + "/owners"))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(groupingAssignmentService, times(1)).groupingImmediateOwners(ADMIN, GROUPING);
    }

    @Test
    @WithMockUhOwner
    public void getAsyncJobResultTest() throws Exception {
        CompletableFuture<?> completableFuture = new CompletableFuture<>();
        Integer jobId = completableFuture.hashCode();
        MvcResult mvcResult = mockMvc.perform(get(API_BASE + "/jobs/" + jobId))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);
        verify(asyncJobsManager, times(1)).getJobResult(TEST_USER, jobId);
    }


    @Test
    @WithMockUhOwner
    public void regexTest() throws Exception {
        // Sending an 'unsafe character' in the URI should get rejected and return SERVER_ERROR
        MvcResult result1 = mockMvc.perform(get(API_BASE + "/owners/" + TEST_USER + "[" + "/groupings"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result1, notNullValue());

        MvcResult result2 = mockMvc.perform(get(API_BASE + "/owners/" + TEST_USER + "^" + "/groupings"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result2, notNullValue());

        MvcResult result3 = mockMvc.perform(get(API_BASE + "/members/" + TEST_USER + "}"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result3, notNullValue());

        MvcResult result4 = mockMvc.perform(get(API_BASE + "/members/" + TEST_USER + "@"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertThat(result4, notNullValue());
    }
}
