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
import edu.hawaii.its.api.groupings.GroupingMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingsService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.UhIdentifierGenerator;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.AsyncJobResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.JsonUtil;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupingsRestControllerv2_1 {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

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
        MvcResult mvcResult = mockMvc.perform(get(API_BASE_URL)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("University of Hawaii Groupings", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void allGroupingsTest() throws Exception {
        String url = API_BASE_URL + "all-groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingPaths.class));
    }

    @Test
    void groupingAdmins() throws Exception {
        String url = API_BASE_URL + "grouping-admins";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingGroupMembers.class));
    }

    @Test
    public void addAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + testUids.get(0);
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingAddResult.class));
        updateMemberService.removeAdminMember(ADMIN, testUids.get(0));
    }

    @Test
    public void removeAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + testUids.get(0);
        updateMemberService.addAdminMember(ADMIN, testUids.get(0));
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResult.class));
        assertFalse(memberService.isAdmin(testUids.get(0)));
    }

    @Test
    public void removeFromGroupsTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(GROUPING_OWNERS);
        pathList.add(GROUPING_INCLUDE);
        testiwtaList.add(testUids.get(0));
        updateMemberService.addOwnerships(ADMIN, GROUPING, testiwtaList);
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testiwtaList);

        String url = API_BASE_URL + "admins/" + String.join(",", pathList) + "/" + testiwtaList.get(0);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));

        assertFalse(memberService.isOwner(GROUPING, testiwtaList.get(0)));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testiwtaList.get(0)));
    }

    @Test
    public void resetIncludeGroupTest() throws Exception {
        assertNotNull(updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingReplaceGroupMembersResult.class));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_INCLUDE, num)));
    }

    @Test
    public void resetIncludeGroupAsyncTest() throws Exception {
        assertNotNull(updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include/async";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url)
                            .header(CURRENT_USER, ADMIN))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_INCLUDE, num)));
    }

    @Test
    public void resetExcludeGroupTest() throws Exception {
        assertNotNull(updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingReplaceGroupMembersResult.class));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    public void resetExcludeGroupAsyncTest() throws Exception {
        assertNotNull(updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude/async";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                Integer.class));

        Integer jobId = Integer.valueOf(mvcResult.getResponse().getContentAsString());
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url)
                            .header(CURRENT_USER, ADMIN))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));

        testUids.forEach(num -> assertFalse(memberService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    public void memberAttributesTest() throws Exception {
        String url = API_BASE_URL + "members/" + testUids.get(0);
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Person.class));
    }

    @Test
    public void memberAttributeResultsTest() throws Exception {
        String url = API_BASE_URL + "members";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), MemberAttributeResults.class));
    }

    @Test
    public void memberAttributeResultsAsyncTest() throws Exception {
        String url = API_BASE_URL + "members/async";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(testUids)))
                .andExpect(status().isAccepted())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));

        String jobId = mvcResult.getResponse().getContentAsString();
        url = API_BASE_URL + "jobs/" + jobId;
        AsyncJobResult asyncJobResult;
        do {
            mvcResult = mockMvc.perform(get(url)
                            .header(CURRENT_USER, ADMIN))
                    .andExpect(status().isOk())
                    .andReturn();
            asyncJobResult = JsonUtil.asObject(mvcResult.getResponse().getContentAsString(), AsyncJobResult.class);
        } while (asyncJobResult.getStatus().equals("IN_PROGRESS"));
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AsyncJobResult.class));
    }

    @Test
    public void ownedGroupingTest() throws Exception {
        String url = API_BASE_URL + "groupings/group?page=1&size=700&sortString=name&isAscending=true";
        List<String> paths = Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE, GROUPING_OWNERS);
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(paths)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult);
        assertNotNull(
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GroupingGroupsMembers.class));
    }

    @Test
    public void membershipResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + ADMIN + "/memberships";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                MembershipResults.class));
    }

    @Test
    public void managePersonResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + testUids.get(0) + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getOptInGroupsTest() throws Exception {
        String url = API_BASE_URL + "groupings/members/" + testUids.get(0) + "/opt-in-groups";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingPaths.class));
    }

    @Test
    public void optInTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        testiwtaList.add(testUids.get(0));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/" + testiwtaList.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMemberResult.class));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, testiwtaList);

    }

    @Test
    public void optOutTest() throws Exception {
        List<String> testiwtaList = new ArrayList<>();
        testiwtaList.add(testUids.get(0));
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testiwtaList);

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + testUids.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingMoveMemberResult.class));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, testiwtaList.get(0)));
    }

    @Test
    public void addIncludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
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
    public void addIncludeMembersAsyncTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/async";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
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
                            .header(CURRENT_USER, ADMIN))
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
    public void addExcludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
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
    public void addExcludeMembersAsyncTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/async";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
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
                            .header(CURRENT_USER, ADMIN))
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
    public void removeIncludeMembersTest() throws Exception {
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
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
    public void removeExcludeMembersTest() throws Exception {
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
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
    public void ownerGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + ADMIN + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), GroupingPaths.class));
    }

    @Test
    public void addOwnersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", testUids);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingAddResults.class));
        testUids.forEach(testUid -> assertTrue(memberService.isOwner(GROUPING, testUid)));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, testUids);
    }

    @Test
    public void removeOwnersTest() throws Exception {
        updateMemberService.addOwnerships(ADMIN, GROUPING, testUids);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", testUids);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingRemoveResults.class));
        testUids.forEach(testUid -> assertFalse(memberService.isOwner(GROUPING, testUid)));
    }

    @Test
    public void updateDescriptionTest() throws Exception {
        String description = groupingsService.getGroupingDescription(GROUPING);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/description";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .content(DEFAULT_DESCRIPTION)) // Add body data.
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingUpdateDescriptionResult.class));
        assertEquals(DEFAULT_DESCRIPTION, groupingsService.getGroupingDescription(GROUPING));
        groupingAttributeService.updateDescription(GROUPING, ADMIN, description);
    }

    @Test
    public void enableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void disableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void enablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.IN.value() + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.OUT.value() + "/enable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + "badPref" + "/enable";
        mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void disablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.IN.value() + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.OUT.value() + "/disable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + "badPref" + "/disable";
        mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        String url = API_BASE_URL + "owners";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        String url = API_BASE_URL + "admins";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void getNumberOfGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + testUids.get(0) + "/groupings/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    public void getNumberOfMembershipsTest() throws Exception {
        String url = API_BASE_URL + "/members/" + testUids.get(0) + "/memberships/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));

    }

    @Test
    public void isSoleOwnerTest() throws Exception {
        String url = API_BASE_URL + "/groupings/" + GROUPING + "/owners/" + CURRENT_USER;
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));

    }

    @Test
    public void groupingOwnersTest() throws Exception {
        String url = API_BASE_URL + "/grouping/" + GROUPING + "/owners";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingGroupMembers.class));
    }

    @Test
    public void getAsyncJobResultTest() throws Exception {
        String url = API_BASE_URL + "jobs/0";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), AsyncJobResult.class));
    }
}