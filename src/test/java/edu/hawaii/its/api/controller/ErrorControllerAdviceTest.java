package edu.hawaii.its.api.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ErrorControllerAdviceTest {

    @Autowired
    private ErrorControllerAdvice errorControllerAdvice;

    @Autowired
    private UserContextService userContextService;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    /**
     * Testing for the ErrorControllerAdvice class.
     * Will Generate a generic exception for each advice method and assert status code values.
     */
    @Test
    public void ErrorControllerTest() {

        User user = userContextService.getCurrentUser();

        assertThat(user.getName(), is(""));

        AccessDeniedException ade = new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        String statusCode = errorControllerAdvice.handleAccessDeniedException(ade).getStatusCode().toString();
        assertThat(statusCode, is("403 FORBIDDEN"));

        Exception e = new Exception("FAIL");
        statusCode = errorControllerAdvice.handleException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        statusCode = errorControllerAdvice.handleMessagingException(e).getStatusCode().toString();
        assertThat(statusCode, is("500 INTERNAL_SERVER_ERROR"));

        RequestRejectedException rre = new RequestRejectedException("FAIL");
        statusCode = errorControllerAdvice.handleRequestRejectedException(rre).getStatusCode().toString();
        assertThat(statusCode, is("400 BAD_REQUEST"));

        GcWebServiceError gwse = new GcWebServiceError("FAIL");
        statusCode = errorControllerAdvice.handleGcWebServiceError(gwse).getStatusCode().toString();
        assertThat(statusCode, is("404 NOT_FOUND"));

        GroupingsServiceResultException gsr = new GroupingsServiceResultException();
        statusCode = errorControllerAdvice.handleGroupingsServiceResultException(gsr).getStatusCode().toString();
        assertThat(statusCode, is("400 BAD_REQUEST"));

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
}
