package edu.hawaii.its.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsAddResults;
import edu.hawaii.its.api.groupings.GroupingsMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingsMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResults;
import edu.hawaii.its.api.groupings.GroupingsReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingsUpdateDescriptionResult;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GroupingsService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.util.JsonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Autowired
    private GrouperApiService grouperApiService;
    @Autowired
    private GroupAttributeService groupAttributeService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UpdateMemberService updateMemberService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MemberService memberService;

    @Autowired GroupingsService groupingsService;

    public static final String API_BASE_URL = "/api/groupings/v2.1/";
    private MockMvc mockMvc;
    private final Map<String, Boolean> attributeMap = new HashMap<>();

    @BeforeAll
    public void init() {
        assertTrue(memberService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> updateMemberService.removeAdmin(ADMIN, testUsername));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        updateMemberService.removeOwnerships(ADMIN, GROUPING, TEST_USERNAMES);

        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberService.isOwner(GROUPING, testUsername));
            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberService.isAdmin(testUsername));
        });

        // Save the starting attribute settings for the test grouping.
        attributeMap.put(OptType.IN.value(), groupAttributeService.isGroupAttribute(GROUPING, OptType.IN.value()));
        attributeMap.put(OptType.OUT.value(), groupAttributeService.isGroupAttribute(GROUPING, OptType.OUT.value()));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(), false);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(), false);

        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @AfterAll
    public void cleanUp() {
        // Set the test grouping's attribute settings back.
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.IN.value(),
                attributeMap.get(OptType.IN.value()));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OptType.OUT.value(),
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
    public void adminsGroupingsTest() throws Exception {
        String url = API_BASE_URL + "admins-and-groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AdminListsHolder.class));
    }

    @Test
    public void addAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + TEST_USERNAMES.get(0);
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsAddResult.class));
        updateMemberService.removeAdmin(ADMIN, TEST_USERNAMES.get(0));
    }

    @Test
    public void removeAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + TEST_USERNAMES.get(0);
        updateMemberService.addAdmin(ADMIN, TEST_USERNAMES.get(0));
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsRemoveResult.class));
        assertFalse(memberService.isAdmin(TEST_USERNAMES.get(0)));
    }

    @Test
    public void removeFromGroupsTest() throws Exception {
        List<String> iamtst01List = new ArrayList<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(GROUPING_OWNERS);
        pathList.add(GROUPING_INCLUDE);
        iamtst01List.add(TEST_USERNAMES.get(0));
        updateMemberService.addOwnerships(ADMIN, GROUPING, iamtst01List);
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);

        String url = API_BASE_URL + "admins/" + String.join(",", pathList) + "/" + iamtst01List.get(0);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsRemoveResults.class));

        assertFalse(memberService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
    }

    @Test
    public void resetIncludeGroup() throws Exception {
        List<String> uhNumbersInclude = TEST_USERNAMES.subList(0, 3);

        assertNotNull(updateMemberService.addIncludeMembers(ADMIN, GROUPING, uhNumbersInclude));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsReplaceGroupMembersResult.class));

        uhNumbersInclude.forEach(num -> assertFalse(memberService.isMember(GROUPING_INCLUDE, num)));
    }

    @Test
    public void resetExcludeGroup() throws Exception {
        List<String> uhNumbersExclude = TEST_USERNAMES.subList(0, 3);

        assertNotNull(updateMemberService.addExcludeMembers(ADMIN, GROUPING, uhNumbersExclude));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsReplaceGroupMembersResult.class));

        uhNumbersExclude.forEach(num -> assertFalse(memberService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    public void invalidUhIdentifiersTest() throws Exception {
        String url = API_BASE_URL + "members/invalid/";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void memberAttributesTest() throws Exception {
        String url = API_BASE_URL + "members/" + TEST_USERNAMES.get(0);
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Person.class));
    }

    @Test
    public void membersAttributesTest() throws Exception {
        String url = API_BASE_URL + "members/";
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getGroupingTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "?page=1&size=1&sortString=name&isAscending=true";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Grouping.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "?";
        mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    public void membershipResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + ADMIN + "/memberships";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                List.class));
    }

    @Test
    public void managePersonResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + TEST_USERNAMES.get(0) + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getOptInGroupsTest() throws Exception {
        String url = API_BASE_URL + "groupings/members/" + TEST_USERNAMES.get(0) + "/opt-in-groups";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void optInTest() throws Exception {
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/" + iamtst01List.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                        GroupingsMoveMemberResult.class));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, iamtst01List);

    }

    @Test
    public void optOutTest() throws Exception {
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + TEST_USERNAMES.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(
                new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                        GroupingsMoveMemberResult.class));
        assertFalse(memberService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
    }

    @Test
    public void addIncludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsMoveMembersResult.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberService.isMember(GROUPING_INCLUDE, username)));
        updateMemberService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
    }

    @Test
    public void addExcludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsMoveMembersResult.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberService.isMember(GROUPING_EXCLUDE, username)));
        updateMemberService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
    }

    @Test
    public void removeIncludeMembersTest() throws Exception {
        updateMemberService.addIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsRemoveResults.class));
        TEST_USERNAMES.forEach(username ->
                assertFalse(memberService.isMember(GROUPING_INCLUDE, username)));
    }

    @Test
    public void removeExcludeMembersTest() throws Exception {
        updateMemberService.addExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/";
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(TEST_USERNAMES)))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsRemoveResults.class));
        TEST_USERNAMES.forEach(username ->
                assertFalse(memberService.isMember(GROUPING_EXCLUDE, username)));
    }

    @Test
    public void ownerGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + ADMIN + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void addOwnersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsAddResults.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberService.isOwner(GROUPING, username)));
        updateMemberService.removeOwnerships(ADMIN, GROUPING, TEST_USERNAMES);
    }

    @Test
    public void removeOwnersTest() throws Exception {
        updateMemberService.addOwnerships(ADMIN, GROUPING, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsRemoveResults.class));
        TEST_USERNAMES.forEach(username -> assertFalse(memberService.isOwner(GROUPING, username)));
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
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsUpdateDescriptionResult.class));
        assertEquals(DEFAULT_DESCRIPTION, groupingsService.getGroupingDescription(GROUPING));
        groupAttributeService.updateDescription(GROUPING, ADMIN, description);
    }

    @Test
    public void enableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void disableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OptType.IN.value() + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void enablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.IN.value() + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.OUT.value() + "/enable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + "badPref" + "/enable";
        try {
            mockMvc.perform(put(url)
                    .header(CURRENT_USER, ADMIN));
            fail("Should not reach here");
        } catch (MissingPathVariableException e) {
            assertNotNull(e);
        }
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void disablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.IN.value() + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OptType.OUT.value() + "/disable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + "badPref" + "/disable";
        try {
            mockMvc.perform(put(url)
                    .header(CURRENT_USER, ADMIN));
            fail("Should not reach here");
        } catch (MissingPathVariableException e) {
            assertNotNull(e);
        }
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getSyncDestinationsTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destinations";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        String url = API_BASE_URL + "owners";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        String url = API_BASE_URL + "admins";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void getNumberOfGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + TEST_USERNAMES.get(0) + "/groupings/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    public void getNumberOfMembershipsTest() throws Exception {
        String url = API_BASE_URL + "/members/" + TEST_USERNAMES.get(0) + "/memberships/count";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    public void isSoleOwnerTest() throws Exception {
        String url = API_BASE_URL + "/groupings/" + GROUPING + "/owners/" + CURRENT_USER;
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }
}

