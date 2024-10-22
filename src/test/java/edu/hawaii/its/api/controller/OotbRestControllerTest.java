package edu.hawaii.its.api.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.type.OotbActiveProfileResult;
import edu.hawaii.its.api.util.JsonUtil;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@ActiveProfiles("ootb")
public class OotbRestControllerTest {

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "path:to:grouping";
    private static final String UID = "user";
    private static final String ADMIN = "admin";
    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;
    @Value("${groupings.api.success}")
    private String SUCCESS;
    @MockBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    @Test
    public void updateActiveDefaultUserTest() throws Exception {
        List<String> paths = Arrays.asList("ROLE_ADMIN", "ROLE_UH", "ROLE_OWNER");

        OotbActiveProfile activeProfile = new OotbActiveProfile();
        activeProfile.setUid("admin0123");
        activeProfile.setUhUuid("33333333");
        activeProfile.setAuthorities(paths);
        activeProfile.setAttributes(new HashMap<>());
        activeProfile.setGroupings(new ArrayList<>());

        OotbActiveProfileResult expectedResult = new OotbActiveProfileResult(activeProfile);

        given(ootbGroupingPropertiesService.updateActiveUserProfile(argThat(profile ->
                profile.getUid().equals(activeProfile.getUid()) &&
                        profile.getUhUuid().equals(activeProfile.getUhUuid()) &&
                        profile.getAuthorities().equals(activeProfile.getAuthorities()) &&
                        profile.getAttributes().equals(activeProfile.getAttributes()) &&
                        profile.getGroupings().equals(activeProfile.getGroupings())
        ))).willReturn(expectedResult);

        MvcResult result = mockMvc.perform(
                        post(API_BASE + "/activeProfile/ootb")
                                .header(CURRENT_USER, CURRENT_USER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.asJson(activeProfile)))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result);
        verify(ootbGroupingPropertiesService, times(1))
                .updateActiveUserProfile(argThat(profile ->
                        profile.getUid().equals(activeProfile.getUid()) &&
                                profile.getUhUuid().equals(activeProfile.getUhUuid()) &&
                                profile.getAuthorities().equals(activeProfile.getAuthorities()) &&
                                profile.getAttributes().equals(activeProfile.getAttributes()) &&
                                profile.getGroupings().equals(activeProfile.getGroupings())
                ));
    }
}