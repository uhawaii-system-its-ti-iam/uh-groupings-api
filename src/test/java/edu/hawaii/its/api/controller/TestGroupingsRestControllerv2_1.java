package edu.hawaii.its.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdateSyncDestResult;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingsService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.UhIdentifierGenerator;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.AsyncJobResult;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.SortBy;
import edu.hawaii.its.api.util.JsonUtil;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsRestControllerv2_1 {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Autowired
    private GroupingAttributeService groupingAttributeService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private UhIdentifierGenerator uhIdentifierGenerator;

    private List<String> testUids;
    public static final String API_BASE_URL = "/api/groupings/v2.1/";
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private final Map<String, Boolean> attributeMap = new HashMap<>();

    @BeforeAll
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));

        testUids = uhIdentifierGenerator.getRandomMembers(4).getUids();
        testUids.forEach(testUid -> updateMemberService.removeAdminMember(ADMIN, testUid));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, testUids);
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUids);

        testUids.forEach(testUid -> {
            assertFalse(memberService.isOwner(GROUPING, testUid));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
            assertFalse(memberService.isAdmin(testUid));
        });

        // Save the starting attribute settings for the test grouping.
        attributeMap.put(OptType.IN.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        attributeMap.put(OptType.OUT.value(), groupingAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);

        mockMvc = webAppContextSetup(webApplicationContext).build();

        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @AfterAll
    public void cleanUp() {
        // Set the test grouping's attribute settings back.
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(),
                attributeMap.get(OptType.IN.value()));
        groupingAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(),
                attributeMap.get(OptType.OUT.value()));
    }

    @Test
    public void helloTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("University of Hawaii Groupings", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void groupingPathIsValidTest() throws Exception {
        String url = API_BASE_URL + "grouping/" + GROUPING + "/is-valid";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Boolean.class));
    }

    @Test
    @WithMockUhAdmin
    public void allGroupingsTest() throws Exception {
        String url = API_BASE_URL + "groupings";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingPaths.class));
    }

    @Test
    @WithMockUhAdmin
    void groupingAdmins() throws Exception {
        String url = API_BASE_URL + "groupings/admins";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingGroupMembers.class));
    }

    @Test
    @WithMockUhAdmin
    public void addAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + testUids.get(0);
        MvcResult mvcResult = mockMvc.perform(post(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingAddResult.class));
        updateMemberService.removeAdminMember(ADMIN, testUids.get(0));
    }

    @Test
    @WithMockUhAdmin
    public void removeAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + testUids.get(0);
        updateMemberService.addAdminMember(ADMIN, testUids.get(0));
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResult.class));
        assertFalse(memberService.isAdmin(testUids.get(0)));
    }

    @Test
    @WithMockUhAdmin
    public void removeFromGroupsTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(GROUPING_OWNERS);
        pathList.add(GROUPING_INCLUDE);
        testiwtaList.add(testUids.get(0));
        updateMemberService.addOwnerships(ADMIN, GROUPING, testiwtaList);
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testiwtaList);

        String url = API_BASE_URL + "admins/" + String.join(",", pathList) + "/" + testiwtaList.get(0);
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));

        assertFalse(memberService.isOwner(GROUPING, testiwtaList.get(0)));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testiwtaList.get(0)));
    }

    @Test
    @WithMockUhAdmin
    public void resetIncludeGroupTest() throws Exception {
        assertNotNull(updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include";
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingReplaceGroupMembersResult.class));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_INCLUDE, num)));
    }

    @Test
    @WithMockUhAdmin
    public void resetIncludeGroupAsyncTest() throws Exception {
        assertNotNull(updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include/async";
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_INCLUDE, num)));
    }

    @Test
    @WithMockUhAdmin
    public void resetExcludeGroupTest() throws Exception {
        assertNotNull(updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude";
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingReplaceGroupMembersResult.class));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    @WithMockUhAdmin
    public void resetExcludeGroupAsyncTest() throws Exception {
        assertNotNull(updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude/async";
        MvcResult mvcResult = mockMvc.perform(delete(url))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        Integer jobId = Integer.valueOf(mvcResult.getResponse().getContentAsString());
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    @WithMockUhAdmin
    public void memberAttributeResultsTest() throws Exception {
        String url = API_BASE_URL + "members";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), MemberAttributeResults.class));
    }

    @Test
    @WithMockUhAdmin
    public void memberAttributeResultsAsyncTest() throws Exception {
        String url = API_BASE_URL + "members/async";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AsyncJobResult.class));
    }

    @Test
    @WithMockUhAdmin
    public void ownedGroupingTest() throws Exception {
        SortBy[] sortByOptions = { SortBy.NAME, SortBy.UID, SortBy.UH_UUID };
        for(SortBy sortBy: sortByOptions){
            String url = API_BASE_URL + "groupings/group?sortBy=" + sortBy.value() + "&page=1&size=700&isAscending=true";
            List<String> paths = Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
            MvcResult mvcResult = mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JsonUtil.asJson(paths)))
                    .andExpect(status().isOk())
                    .andReturn();
            assertNotNull(mvcResult);
            assertNotNull(
                    objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupingGroupsMembers.class));
        }
    }

    @Test
    @WithMockUhAdmin
    public void getGroupingMembersTest() throws Exception {
        SortBy[] sortByOptions = { SortBy.NAME, SortBy.UID, SortBy.UH_UUID };
        for(SortBy sortBy: sortByOptions){
            String url = API_BASE_URL + "groupings/group?sortBy=" + sortBy.value()
                    + "&page=1&size=700&isAscending=true&searchString=test";
            List<String> paths = Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
            MvcResult mvcResult = mockMvc.perform(post(url)
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JsonUtil.asJson(paths)))
                    .andExpect(status().isOk())
                    .andReturn();
            assertNotNull(mvcResult);
            assertNotNull(
                    objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupingGroupsMembers.class));
        }

    }

    @Test
    @WithMockUhAdmin
    public void getGroupingMembersWhereListedTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/where-listed";
        List<String> members = Arrays.asList("testiwta", "testiwtb");
        MvcResult mvcResult = mockMvc.perform(post(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupingMembers.class));
    }

    @Test
    @WithMockUhAdmin
    public void getGroupingMembersIsBasisTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/is-basis";
        List<String> members = Arrays.asList("testiwta", "testiwtb");
        MvcResult mvcResult = mockMvc.perform(post(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(members)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupingMembers.class));
    }

    @Test
    @WithMockUhAdmin
    public void membershipResultsTest() throws Exception {
        String url = API_BASE_URL + "members/memberships";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                MembershipResults.class));
    }

    @Test
    @WithMockUhAdmin
    public void manageSubjectResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + testUids.get(0) + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), ManageSubjectResults.class));
    }

    @Test
    @WithMockUhAdmin
    public void getOptInGroupsTest() throws Exception {
        String url = API_BASE_URL + "groupings/members/" + testUids.get(0) + "/opt-in-groups";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingPaths.class));
    }

    @Test
    @WithMockUhAdmin
    public void optInTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        testiwtaList.add(testUids.get(0));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/" + testiwtaList.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMemberResult.class));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testiwtaList);

    }

    @Test
    @WithMockUhAdmin
    public void optOutTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        testiwtaList.add(testUids.get(0));
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testiwtaList);

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + testUids.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMemberResult.class));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testiwtaList.get(0)));
    }

    @Test
    @WithMockUhAdmin
    public void addIncludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMembersResult.class));
        testUids.forEach(testUid -> assertTrue(memberService.isMember(GROUPING_INCLUDE, testUid)));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    @WithMockUhAdmin
    public void addIncludeMembersAsyncTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/async";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url)
                            )
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AsyncJobResult.class));

        testUids.forEach(testUid -> assertTrue(memberService.isMember(GROUPING_INCLUDE, testUid)));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    @WithMockUhAdmin
    public void addExcludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMembersResult.class));
        testUids.forEach(testUid -> assertTrue(memberService.isMember(GROUPING_EXCLUDE, testUid)));
        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    @WithMockUhAdmin
    public void addExcludeMembersAsyncTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/async";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AsyncJobResult.class));

        testUids.forEach(testUid -> assertTrue(memberService.isMember(GROUPING_EXCLUDE, testUid)));
        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, testUids);
    }

    @Test
    @WithMockUhAdmin
    public void removeIncludeMembersTest() throws Exception {
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));
        testUids.forEach(testUid ->
                assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid)));
    }

    @Test
    @WithMockUhAdmin
    public void removeExcludeMembersTest() throws Exception {
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));
        testUids.forEach(testUid ->
                assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid)));
    }

    @Test
    @WithMockUhAdmin
    public void ownerGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingPaths.class));
    }

    @Test
    @WithMockUhAdmin
    public void addOwnersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", testUids);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingAddResults.class));
        testUids.forEach(testUid -> assertTrue(memberService.isOwner(GROUPING, testUid)));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUids);
    }

    @Test
    @WithMockUhAdmin
    public void addOwnerGroupingsTest() throws Exception {
        String groupingPath = String.format("tmp:%s:%s-single", ADMIN, ADMIN);
        List<String> ownerGroupingsToAdd = new ArrayList<>();
        ownerGroupingsToAdd.add(String.format("tmp:%s:%s-aux", ADMIN, ADMIN));
        ownerGroupingsToAdd.add(String.format("tmp:%s:%s-complex", ADMIN, ADMIN));

        String url = API_BASE_URL + "groupings/" + groupingPath + "/owners/owner-groupings/" + String.join(",", ownerGroupingsToAdd);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingAddResults.class));
        ownerGroupingsToAdd.forEach(ownerGrouping -> assertTrue(memberService.isOwner(groupingPath, ownerGrouping)));
        updateMemberService.removeOwnerGroupingOwnerships(ADMIN, groupingPath, ownerGroupingsToAdd);
    }

    @Test
    @WithMockUhAdmin
    public void removeOwnersTest() throws Exception {
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", testUids);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));
        testUids.forEach(testUid -> assertFalse(memberService.isOwner(GROUPING, testUid)));
    }

    @Test
    @WithMockUhAdmin
    public void removeOwnerGroupingsTest() throws Exception {
        String groupingPath = String.format("tmp:%s:%s-single", ADMIN, ADMIN);
        List<String> ownerGroupingsToAdd = new ArrayList<>();
        ownerGroupingsToAdd.add(String.format("tmp:%s:%s-aux", ADMIN, ADMIN));
        ownerGroupingsToAdd.add(String.format("tmp:%s:%s-complex", ADMIN, ADMIN));
        updateMemberService.addOwnerGroupingOwnerships(ADMIN, groupingPath, ownerGroupingsToAdd);
        String url = API_BASE_URL + "groupings/" + groupingPath + "/owners/owner-groupings/" + String.join(",", ownerGroupingsToAdd);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));
        ownerGroupingsToAdd.forEach(ownerGrouping -> assertFalse(memberService.isOwner(groupingPath, ownerGrouping)));
    }

    @Test
    @WithMockUhAdmin
    public void updateDescriptionTest() throws Exception {
        String description = groupingsService.getGroupingDescription(GROUPING);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/description";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        
                        .content(DEFAULT_DESCRIPTION)) // Add body data.
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingUpdateDescriptionResult.class));
        assertEquals(DEFAULT_DESCRIPTION, groupingsService.getGroupingDescription(GROUPING));
        groupingAttributeService.updateDescription(GROUPING, ADMIN, description);
    }

    @Test
    @WithMockUhAdmin
    public void updateSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/true";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();

        GroupingUpdateSyncDestResult result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingUpdateSyncDestResult.class);
        assertNotNull(result);

        url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/false";
        mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();

        result = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingUpdateSyncDestResult.class);
        assertNotNull(result);

        url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + "badSyncDest" + "/enable";
        mockMvc.perform(put(url)
                        )
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUhAdmin
    public void updateOptAttributeTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/opt-attribute/" + OptType.IN.value() + "/true";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();

        String result = mvcResult.getResponse().getContentAsString();
        assertNotNull(result);

        url = API_BASE_URL + "groupings/" + GROUPING + "/opt-attribute/" + OptType.IN.value() + "/false";
        mvcResult = mockMvc.perform(put(url)
                        )
                .andExpect(status().isOk())
                .andReturn();

        result = mvcResult.getResponse().getContentAsString();
        assertNotNull(result);

        url = API_BASE_URL + "groupings/" + GROUPING + "/opt-attribute/" + "badOpt" + "/true";
        mockMvc.perform(put(url)
                        )
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        String url = API_BASE_URL + "members/" + ADMIN + "/is-admin";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        String url = API_BASE_URL + "members/" + ADMIN + "/is-owner";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void hasGroupingOwnerPrivsTest() throws Exception {
        String url = API_BASE_URL + "members/" + GROUPING + "/" + ADMIN + "/is-owner";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/groupings/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfMembershipsTest() throws Exception {
        String url = API_BASE_URL + "/members/memberships/count";
        MvcResult mvcResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));

    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfGroupingMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfOwnersTest() throws Exception {
        String url = API_BASE_URL + "/members/" + GROUPING + "/owners/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    @WithMockUhAdmin
    public void getNumberOfAllOwnersTest() throws Exception {
        String url = API_BASE_URL + "/groupings/" + GROUPING + "/owners/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    @WithMockUhAdmin
    public void compareOwnerGroupingsTest() throws Exception {
        String url = API_BASE_URL + "/groupings/" + GROUPING + "/owners/compare";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Map<String, Object>>>() {}));
    }

    @Test
    @WithMockUhAdmin
    public void groupingOwnersTest() throws Exception {
        String url = API_BASE_URL + "/grouping/" + GROUPING + "/owners";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingGroupMembers.class));
    }

    @Test
    @WithMockUhAdmin
    public void getAsyncJobResultTest() throws Exception {
        String url = API_BASE_URL + "jobs/0";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        )
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), AsyncJobResult.class));
    }
}