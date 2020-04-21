package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.mail.MessagingException;
import java.io.IOException;

@ControllerAdvice
public class ErrorControllerAdvice {

    private static final Log logger = LogFactory.getLog(ErrorControllerAdvice.class);

    @Autowired
    private UserContextService userContextService;

    @ExceptionHandler(GcWebServiceError.class)
    public ResponseEntity<GroupingsHTTPException>
    handleGcWebServiceError(GcWebServiceError gce) {
        return exceptionResponse(gce.getMessage(), gce, 404);
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<GroupingsHTTPException> handleRequestRejectedException(RequestRejectedException rre) {
        return exceptionResponse(rre.getMessage(), rre, 400);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GroupingsHTTPException> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException hrmnse) {
        return exceptionResponse(hrmnse.getMessage(), hrmnse, 405);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GroupingsHTTPException> handleIllegalArgumentException(IllegalArgumentException iae) {
        return exceptionResponse("Resource not available", iae, 404);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GroupingsHTTPException> handleRuntimeException(RuntimeException re) {
        return exceptionResponse("runtime exception", re, 500);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<GroupingsHTTPException> handleUnsupportedOperationException(
            UnsupportedOperationException nie) {
        return exceptionResponse("Method not implemented", nie, 501);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GroupingsHTTPException> handleException(Exception exception) {
        return exceptionResponse("Exception", exception, 500);
    }

    @ExceptionHandler({MessagingException.class, IOException.class})
    public ResponseEntity<GroupingsHTTPException> handleMessagingException(Exception e) {
      return exceptionResponse("Mail service exception", e, 500);
    }

    @ExceptionHandler(GroupingsServiceResultException.class)
    public ResponseEntity<GroupingsHTTPException> handleGroupingsServiceResultException(
            GroupingsServiceResultException gsre) {
        ResponseEntity<GroupingsHTTPException> re = exceptionResponse("Groupings Service resulted in FAILURE", gsre, 400);
        return re;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GroupingsHTTPException> handleAccessDeniedException(
            AccessDeniedException ade) {
        ResponseEntity<GroupingsHTTPException> re = exceptionResponse("The current user does not have permission to " +
                "preform this action.", ade, 403);
        return re;
    }

    private ResponseEntity<GroupingsHTTPException> exceptionResponse(String message, Throwable cause, int status) {
        String username = null;
        User user = userContextService.getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        }

        GroupingsHTTPException httpException = new GroupingsHTTPException(message, cause, status);

        logger.error("username: " + username + "; Exception: ", httpException.getCause());

        return ResponseEntity.status(status).body(httpException);
    }
}
