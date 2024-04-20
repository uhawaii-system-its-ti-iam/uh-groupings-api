package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.AsyncJobsManager;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.service.GroupingOwnerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ErrorControllerAdviceTest {

    @Autowired
    private ErrorControllerAdvice errorControllerAdvice;

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

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "grouping";
    private static final String UID = "user";
    private static final String ADMIN = "admin";

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    /**
     * Testing for the ErrorControllerAdvice class.
     * Will Generate a generic exception for each advice method and assert status code values.
     */
    @Test
    public void ErrorControllerTest() {

        AccessDeniedException ade = new AccessDeniedException();
        String statusCode = errorControllerAdvice.handleAccessDeniedException(ade).getStatusCode().toString();
        assertThat(statusCode, is("403 FORBIDDEN"));

        Exception e = new Exception("FAIL");
        statusCode = errorControllerAdvice.handleException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        statusCode = errorControllerAdvice.handleMessagingException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        GcWebServiceError gwse = new GcWebServiceError("FAIL");
        statusCode = errorControllerAdvice.handleGcWebServiceError(gwse).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        GroupingsServiceResultException gsr = new GroupingsServiceResultException();
        statusCode = errorControllerAdvice.handleGroupingsServiceResultException(gsr).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        IllegalArgumentException iae = new IllegalArgumentException();
        statusCode = errorControllerAdvice.handleIllegalArgumentException(iae).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        HttpRequestMethodNotSupportedException hrmnse = new HttpRequestMethodNotSupportedException("FAIL");
        statusCode = errorControllerAdvice.handleHttpRequestMethodNotSupportedException(hrmnse).getStatusCode().toString();
        assertThat(statusCode, is("405 METHOD_NOT_ALLOWED"));

        UnsupportedOperationException uoe = new UnsupportedOperationException();
        statusCode = errorControllerAdvice.handleUnsupportedOperationException(uoe).getStatusCode().toString();
        assertThat(statusCode, is("501 NOT_IMPLEMENTED"));
    }

    @Test
    public void testMembershipResultsExceptionHandling() throws Exception {
        String uhIdentifier = "1234";

        //when current_user and uhIdentifier is same, but uhIdentifier is not valid
        given(membershipService.membershipResults(uhIdentifier, uhIdentifier)).willThrow(UhMemberNotFoundException.class);

        MvcResult result = mockMvc.perform(get(API_BASE + "/members/{uhIdentifier}/memberships", uhIdentifier)
                        .header(CURRENT_USER, uhIdentifier))
                .andExpect(status().isNotFound()) // Checking for a 404 status
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("UH Member found failed"))
                .andExpect(jsonPath("$.debugMessage").value("This is not the validation error from frontend, check Sub Error to see the reason in the backend"))
                .andExpect(jsonPath("$.subErrors").isArray()) // Checking if subErrors is an array
                .andExpect(jsonPath("$.subErrors", hasSize(0))) // Checking if subErrors array is empty
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(result, notNullValue());
        assertTrue(content.contains("NOT_FOUND"));
    }
    @Test
    public void throwExceptionTest() throws Exception {
        MvcResult result = mockMvc.perform(get(API_BASE + "/testing/exception"))
                .andExpect(status().isInternalServerError()) // Checking for a 500 status
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.debugMessage").value("Check your service at endpoint /exception"))
                .andExpect(jsonPath("$.subErrors").isArray()) // Checking if subErrors is an array
                .andExpect(jsonPath("$.subErrors", hasSize(2))) // Checking if subErrors array has 2 elements
                .andExpect(jsonPath("$.subErrors[0].object").value("testing object 1"))
                .andExpect(jsonPath("$.subErrors[0].field").value("membership service"))
                .andExpect(jsonPath("$.subErrors[0].rejectedValue").value("id: 12"))
                .andExpect(jsonPath("$.subErrors[0].message").value("Membership access denied"))
                .andExpect(jsonPath("$.subErrors[1].object").value("testing object 2"))
                .andExpect(jsonPath("$.subErrors[1].field").value("member service"))
                .andExpect(jsonPath("$.subErrors[1].rejectedValue").value("id: 30"))
                .andExpect(jsonPath("$.subErrors[1].message").value("There is no member"))
                .andReturn();

        assertThat(result, notNullValue());
    }
}
