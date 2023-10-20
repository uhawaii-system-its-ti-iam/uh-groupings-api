package edu.hawaii.its.api.controller;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class ErrorControllerAdviceTest {

    @Autowired
    private ErrorControllerAdvice errorControllerAdvice;

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
