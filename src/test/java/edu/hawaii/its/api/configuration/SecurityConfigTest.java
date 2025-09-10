package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.filter.JwtAuthenticationFilter;
import edu.hawaii.its.api.service.AnnouncementsService;
import edu.hawaii.its.api.type.Announcements;

@SpringBootTest(classes = {SpringBootWebApplication.class})
public class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AnnouncementsService announcementsService;

    private MockMvc mockMvc;

    public static final String API_BASE_URL = "/api/groupings/v2.1/";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void construction() {
        assertNotNull(securityConfig);
    }

    @Test
    public void securityFilterChainBeanExists() {
        assertNotNull(securityFilterChain);
    }

    @Test
    public void jwtAuthenticationFilterIsConfigured() {
        assertNotNull(jwtAuthenticationFilter);
    }

    @Test
    public void publicEndpointsAreAccessibleTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v3/api-docs"))
                        .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());

        mvcResult = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());

        given(announcementsService.getAnnouncements()).willReturn(new Announcements());

        mvcResult = mockMvc.perform(get(API_BASE_URL + "announcements"))
                .andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void protectedEndpointRequiresAuthenticationTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(API_BASE_URL + "members/memberships"))
                .andReturn();
        assertEquals(403, mvcResult.getResponse().getStatus());
    }

    @Test
    public void protectedEndpointWithMissingBearerTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(API_BASE_URL + "members/memberships")
                        .header("Authorization", "InvalidFormat"))
                .andReturn();
        assertEquals(403, mvcResult.getResponse().getStatus());
    }
}