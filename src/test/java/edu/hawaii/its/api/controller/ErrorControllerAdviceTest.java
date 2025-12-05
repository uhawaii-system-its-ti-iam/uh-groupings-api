package edu.hawaii.its.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import edu.hawaii.its.api.exception.DirectOwnerRemovedException;
import edu.hawaii.its.api.exception.OwnerLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.service.AsyncJobsManager;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingOwnerService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.UpdateMemberService;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ErrorControllerAdviceTest {

    @Autowired
    private ErrorControllerAdvice errorControllerAdvice;

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
    private MemberService memberService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String API_BASE = "/api/groupings/v2.1";
    private static final String GROUPING = "grouping";
    private static final String TEST_USER = "test_user";
    private static final String ADMIN = "admin";

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    /**
     * Testing for the ErrorControllerAdvice class.
     * Will generate a generic exception for each advice method and assert status code values.
     */
    @Test
    public void testErrorController() {
        DirectOwnerRemovedException dore = new DirectOwnerRemovedException();
        String statusCode = errorControllerAdvice.handleDirectOwnerRemovedException(dore).getStatusCode().toString();
        assertThat(statusCode, is("422 UNPROCESSABLE_ENTITY"));

        OwnerLimitExceededException olee = new OwnerLimitExceededException();
        statusCode = errorControllerAdvice.handleOwnerLimitExceededException(olee).getStatusCode().toString();
        assertThat(statusCode, is("409 CONFLICT"));

        AccessDeniedException ade = new AccessDeniedException();
        statusCode = errorControllerAdvice.handleAccessDeniedException(ade).getStatusCode().toString();
        assertThat(statusCode, is("403 FORBIDDEN"));

        IllegalArgumentException iae = new IllegalArgumentException();
        statusCode = errorControllerAdvice.handleIllegalArgumentException(iae).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        HttpRequestMethodNotSupportedException hrmnse = new HttpRequestMethodNotSupportedException("FAIL");
        statusCode =
                errorControllerAdvice.handleHttpRequestMethodNotSupportedException(hrmnse).getStatusCode().toString();
        assertThat(statusCode, is("405 METHOD_NOT_ALLOWED"));

        Exception e = new Exception("FAIL");
        statusCode = errorControllerAdvice.handleException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));
        
        statusCode = errorControllerAdvice.handleMessagingException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        UnsupportedOperationException uoe = new UnsupportedOperationException();
        statusCode = errorControllerAdvice.handleUnsupportedOperationException(uoe).getStatusCode().toString();
        assertThat(statusCode, is("501 NOT_IMPLEMENTED"));

        InvalidGroupPathException igpe = new InvalidGroupPathException("Invalid Group Path Exception");
        statusCode = errorControllerAdvice.handleInvalidGroupPathException(igpe).getStatusCode().toString();
        assertThat(statusCode, is("400 BAD_REQUEST"));
    }

    @Test
    @WithMockUhOwner
    public void testMembershipResultsExceptionHandling() throws Exception {
        // When current_user and uhIdentifier are the same, but uhIdentifier is not valid
        given(membershipService.membershipResults(TEST_USER)).willThrow(UhIdentifierNotFoundException.class);

        MvcResult result = mockMvc.perform(get(API_BASE + "/members/memberships"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.resultCode").value("FAILURE"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("UH Member found failed"))
                .andExpect(jsonPath("$.path").value("/api/groupings/v2.1/members/memberships"))
                .andExpect(jsonPath("$.stackTrace").exists())

                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(result, notNullValue());
        assertTrue(content.contains("NOT_FOUND"));
    }
}
