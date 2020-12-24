package edu.hawaii.its.api.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.access.AuthorizationService;
import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingServiceResultExceptionTest;
import edu.hawaii.its.api.type.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ErrorControllerAdviceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ErrorControllerAdvice errorControllerAdvice;

    @Autowired
    private UserContextService userContextService;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Before
    public void setUp() {
        webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void ErrorControllerTest() {
        User user = userContextService.getCurrentUser();

        assertThat(user.getName(), is(""));

        AccessDeniedException ade = new AccessDeniedException(INSUFFICIENT_PRIVILEGES);

        String statusCode = errorControllerAdvice.handleAccessDeniedException(ade).getStatusCode().toString();
        assertThat(statusCode, is("403 FORBIDDEN"));

        statusCode = errorControllerAdvice.handleException(new Exception("FAIL")).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        statusCode = errorControllerAdvice.handleMessagingException(new Exception("FAIL")).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        statusCode = errorControllerAdvice.handleRequestRejectedException(new RequestRejectedException("FAIL")).getStatusCode().toString();
        assertThat(statusCode, is("400 BAD_REQUEST"));

        statusCode = errorControllerAdvice.handleGcWebServiceError(new GcWebServiceError("FAIL")).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        statusCode = errorControllerAdvice.handleGroupingsServiceResultException(new GroupingsServiceResultException()).getStatusCode().toString();
        assertThat(statusCode, is("400 BAD_REQUEST"));

        statusCode = errorControllerAdvice.handleIllegalArgumentException(new IllegalArgumentException()).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        statusCode = errorControllerAdvice.handleHttpRequestMethodNotSupportedException(new HttpRequestMethodNotSupportedException("FAIL")).getStatusCode().toString();
        assertThat(statusCode, is("405 METHOD_NOT_ALLOWED"));

        statusCode = errorControllerAdvice.handleUnsupportedOperationException(new UnsupportedOperationException()).getStatusCode().toString();
        assertThat(statusCode, is("501 NOT_IMPLEMENTED"));
    }
}
