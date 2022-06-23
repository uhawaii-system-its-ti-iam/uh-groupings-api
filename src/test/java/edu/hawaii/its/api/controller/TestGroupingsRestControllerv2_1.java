package edu.hawaii.its.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.test.usernames}")
    private List<String> TEST_USERNAMES;

    @Value("Test Many Groups In Basis")
    private String DEFAULT_DESCRIPTION;

    @Autowired
    private GrouperApiService grouperApiService;
    @Autowired
    private MemberAttributeService memberAttributeService;
    @Autowired
    private MembershipService membershipService;
    @Autowired
    private GroupAttributeService groupAttributeService;
    @Autowired
    private WebApplicationContext webApplicationContext;

    public static final String API_BASE_URL = "/api/groupings/v2.1/";
    private MockMvc mockMvc;
    private final Map<String, Boolean> attributeMap = new HashMap<>();

    @BeforeAll
    public void init() {
        assertTrue(memberAttributeService.isAdmin(ADMIN));
        TEST_USERNAMES.forEach(testUsername -> membershipService.removeAdmin(ADMIN, testUsername));
        membershipService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        membershipService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);

        TEST_USERNAMES.forEach(testUsername -> {
            assertFalse(memberAttributeService.isOwner(GROUPING, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, testUsername));
            assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, testUsername));
            assertFalse(memberAttributeService.isAdmin(testUsername));
        });

        // Save the starting attribute settings for the test grouping.
        attributeMap.put(OPT_IN, groupAttributeService.isGroupAttribute(GROUPING, OPT_IN));
        attributeMap.put(OPT_OUT, groupAttributeService.isGroupAttribute(GROUPING, OPT_OUT));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OPT_IN, false);
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OPT_OUT, false);

        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @AfterAll
    public void cleanUp() {
        // Set the test grouping's attribute settings back.
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OPT_IN, attributeMap.get(OPT_IN));
        groupAttributeService.changeGroupAttributeStatus(GROUPING, ADMIN, OPT_OUT, attributeMap.get(OPT_OUT));
    }

    @Test
    public void helloTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(API_BASE_URL)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("University of Hawaii Groupings", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void adminsGroupingsTest() throws Exception {
        String url = API_BASE_URL + "admins-and-groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AdminListsHolder.class));
    }

    @Test
    public void addAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + TEST_USERNAMES.get(0);
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                AddMemberResult.class));
        membershipService.removeAdmin(ADMIN, TEST_USERNAMES.get(0));
    }

    @Test
    public void removeAdminTest() throws Exception {
        String url = API_BASE_URL + "admins/" + TEST_USERNAMES.get(0);
        membershipService.addAdmin(ADMIN, TEST_USERNAMES.get(0));
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                RemoveMemberResult.class));
        assertFalse(memberAttributeService.isAdmin(TEST_USERNAMES.get(0)));
    }

    @Test
    public void removeFromGroupsTest() throws Exception {
        List<String> iamtst01List = new ArrayList<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(GROUPING_OWNERS);
        pathList.add(GROUPING_INCLUDE);
        iamtst01List.add(TEST_USERNAMES.get(0));
        membershipService.addOwnerships(GROUPING, ADMIN, iamtst01List);
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);

        String url = API_BASE_URL + "admins/" + String.join(",", pathList) + "/" + iamtst01List.get(0);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        assertFalse(memberAttributeService.isOwner(GROUPING, iamtst01List.get(0)));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
    }

    @Test
    public void resetGroupTest() throws Exception {
        List<String> uhNumbersInclude = TEST_USERNAMES.subList(0, 3);
        List<String> uhNumbersExclude = TEST_USERNAMES.subList(3, 6);
        assertNotNull(membershipService.addIncludeMembers(ADMIN, GROUPING, uhNumbersInclude));
        assertNotNull(membershipService.addExcludeMembers(ADMIN, GROUPING, uhNumbersExclude));

        String url = API_BASE_URL + "groupings/" + GROUPING + "/" +
                String.join(",", uhNumbersInclude) + "/" +
                String.join(",", uhNumbersExclude) + "/reset-group";

        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        uhNumbersInclude.forEach(num -> assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, num)));
        uhNumbersExclude.forEach(num -> assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, num)));
    }

    @Test
    public void memberAttributesTest() throws Exception {
        String url = API_BASE_URL + "members/" + TEST_USERNAMES.get(0);
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Person.class));
    }

    @Test
    public void getGroupingTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "?page=1&size=1&sortString=name&isAscending=true";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Grouping.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "?";
        mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    public void membershipResultsTest() throws Exception {
        String url = API_BASE_URL + "members/" + ADMIN + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getOptInGroupsTest() throws Exception {
        String url = API_BASE_URL + "groupings/members/" + TEST_USERNAMES.get(0) + "/opt-in-groups";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
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
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        membershipService.removeIncludeMembers(ADMIN, GROUPING, iamtst01List);

    }

    @Test
    public void optOutTest() throws Exception {
        List<String> iamtst01List = new ArrayList<>();
        iamtst01List.add(TEST_USERNAMES.get(0));
        membershipService.addIncludeMembers(ADMIN, GROUPING, iamtst01List);

        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + TEST_USERNAMES.get(0) + "/self";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, iamtst01List.get(0)));
    }

    @Test
    public void addIncludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username)));
        membershipService.removeIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
    }

    @Test
    public void addExcludeMembersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username)));
        membershipService.removeExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
    }

    @Test
    public void removeIncludeMembersTest() throws Exception {
        membershipService.addIncludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/include-members/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username ->
                assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username)));
    }

    @Test
    public void removeExcludeMembersTest() throws Exception {
        membershipService.addExcludeMembers(ADMIN, GROUPING, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/exclude-members/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username ->
                assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username)));
    }

    @Test
    public void ownerGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + ADMIN + "/groupings";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void addOwnersTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username -> assertTrue(memberAttributeService.isOwner(GROUPING, username)));
        membershipService.removeOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
    }

    @Test
    public void removeOwnersTest() throws Exception {
        membershipService.addOwnerships(GROUPING, ADMIN, TEST_USERNAMES);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/owners/" + String.join(",", TEST_USERNAMES);
        MvcResult mvcResult = mockMvc.perform(delete(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
        TEST_USERNAMES.forEach(username -> assertFalse(memberAttributeService.isOwner(GROUPING, username)));
    }

    @Test
    public void updateDescriptionTest() throws Exception {
        String description = grouperApiService.descriptionOf(GROUPING);
        String url = API_BASE_URL + "groupings/" + GROUPING + "/description";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .content(DEFAULT_DESCRIPTION) // Add body data.
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
        assertEquals(DEFAULT_DESCRIPTION, grouperApiService.descriptionOf(GROUPING));
        groupAttributeService.updateDescription(GROUPING, ADMIN, description);
    }

    @Test
    public void enableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OPT_IN + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void disableSyncDestTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destination/" + OPT_IN + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(),
                GroupingsServiceResult.class));
    }

    @Test
    public void enablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OPT_IN + "/enable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OPT_OUT + "/enable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + "badPref" + "/enable";
        try {
            mockMvc.perform(put(url)
                            .header(CURRENT_USER, ADMIN)
                            .with(user(ADMIN))
                            .with(csrf()))
                    .andExpect(status().is(501))
                    .andReturn();
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void disablePreferenceTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OPT_IN + "/disable";
        MvcResult mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));

        url = API_BASE_URL + "groupings/" + GROUPING + "/preference/" + OPT_OUT + "/disable";
        mvcResult = mockMvc.perform(put(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void getSyncDestinationsTest() throws Exception {
        String url = API_BASE_URL + "groupings/" + GROUPING + "/sync-destinations";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), List.class));
    }

    @Test
    public void hasAdminPrivsTest() throws Exception {
        String url = API_BASE_URL + "owners";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void hasOwnerPrivsTest() throws Exception {
        String url = API_BASE_URL + "admins";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }

    @Test
    public void getNumberOfGroupingsTest() throws Exception {
        String url = API_BASE_URL + "owners/" + TEST_USERNAMES.get(0) + "/grouping";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    public void getNumberOfMembershipsTest() throws Exception {
        String url = API_BASE_URL + "groupings/members/" + TEST_USERNAMES.get(0) + "/memberships";
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Integer.class));
    }

    @Test
    public void isSoleOwnerTest() throws Exception {
        String url = API_BASE_URL + "/groupings/" + GROUPING + "/owners/" + CURRENT_USER;
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header(CURRENT_USER, ADMIN)
                        .with(user(ADMIN)).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(new ObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), Boolean.class));
    }
}

