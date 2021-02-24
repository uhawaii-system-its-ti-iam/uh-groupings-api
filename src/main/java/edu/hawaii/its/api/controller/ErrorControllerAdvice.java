package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;
import edu.hawaii.its.api.service.EmailService;
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

    @Autowired
    private EmailService emailService;

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<GroupingsHTTPException> handleRequestRejectedException(RequestRejectedException rre) {
        emailService.sendWithStack(rre, "Request Rejected Exception");
        return exceptionResponse(rre.getMessage(), rre, 400);
    }

    @ExceptionHandler(GroupingsServiceResultException.class)
    public ResponseEntity<GroupingsHTTPException> handleGroupingsServiceResultException(
        GroupingsServiceResultException gsre) {
        emailService.sendWithStack(gsre, "Groupings ServiceResult Exception");
      return exceptionResponse("Groupings Service resulted in FAILURE", gsre, 400);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GroupingsHTTPException> handleAccessDeniedException(AccessDeniedException ade) {
      emailService.sendWithStack(ade, "Access Denied Exception");
      return exceptionResponse("The current user does not have permission to " +
              "perform this action.", ade, 403);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GroupingsHTTPException> handleIllegalArgumentException(IllegalArgumentException iae) {
        emailService.sendWithStack(iae, "Illegal Argument Exception");
        return exceptionResponse("Resource not available", iae, 404);
    }

    @ExceptionHandler(GcWebServiceError.class)
    public ResponseEntity<GroupingsHTTPException> handleGcWebServiceError(GcWebServiceError gce) {
        emailService.sendWithStack(gce, "Gc Web Service Error");
      return exceptionResponse(gce.getMessage(), gce, 404);
  }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GroupingsHTTPException> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException hrmnse) {
        emailService.sendWithStack(hrmnse, "Http Request Method Not Supported Exception");
      return exceptionResponse(hrmnse.getMessage(), hrmnse, 405);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<GroupingsHTTPException> handleException(Exception exception) {
        emailService.sendWithStack(exception, "Runtime Exception");
        return exceptionResponse("Exception", exception, 500);
    }

    @ExceptionHandler({MessagingException.class, IOException.class})
    public ResponseEntity<GroupingsHTTPException> handleMessagingException(Exception e) {
        emailService.sendWithStack(e, "Messaging Exception");
      return exceptionResponse("Mail service exception", e, 500);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<GroupingsHTTPException> handleUnsupportedOperationException(
        UnsupportedOperationException nie) {
        emailService.sendWithStack(nie, "Unsupported Operation Exception");
      return exceptionResponse("Method not implemented", nie, 501);
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
