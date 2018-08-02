package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.access.User;
import edu.hawaii.its.api.access.UserContextService;

@ControllerAdvice
public class ErrorControllerAdvice {

    // TODO: change this to HATEOAS style
    //  Do we need to use @ResponseBody for the methods or is this already being handled?
    //  Do we need to use @ResponseStatus for the methods or is the HTTP status already being added?
    //  Should we use VndErrors() type and get rid of the Groupings specific error types?
    // Looking into it, not sure if HATEOAS is doable/necessary for Exception Handling

    //todo Do we need seperate exceptions for unauthorized operations?

    private static final Log logger = LogFactory.getLog(ErrorControllerAdvice.class);

    @Autowired
    private UserContextService userContextService;

    //todo Not tested
    //todo Handler for .../{Empty fields}/{Method} doesn't work still returns 500 should return 400
    //todo Implementing StrictHttpFirewall override, not sure if this is the right thing to do security-wise
    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<GroupingsHTTPException> handleRequestRejectedException(RequestRejectedException rre) {
        return exceptionResponse(rre.getMessage(), rre, 400);
    }

    //todo Not tested
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GroupingsHTTPException> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException hrmnse) {
        return exceptionResponse(hrmnse.getMessage(), hrmnse, 405);
    }

    //todo Not tested
    //todo Possibly could return 400 (bad request) instead
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GroupingsHTTPException> handleIllegalArgumentException(IllegalArgumentException iae,
            WebRequest request) {
        return exceptionResponse("Resource not available", iae, 404);
    }

    //todo Not tested
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GroupingsHTTPException> handleRuntimeException(RuntimeException re) {
        return exceptionResponse("runtime exception", re, 500);
    }

    //todo Not tested
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<GroupingsHTTPException> handleUnsupportedOperationException(
            UnsupportedOperationException nie) {
        return exceptionResponse("Method not implemented", nie, 501);
    }

    //todo Not tested
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GroupingsHTTPException> handleException(Exception exception) {
        return exceptionResponse("Exception", exception, 500);
    }

    //todo Not tested
    @ExceptionHandler(GroupingsServiceResultException.class)
    public ResponseEntity<GroupingsHTTPException> handleGroupingsServiceResultException(
            GroupingsServiceResultException gsre) {
        ResponseEntity<GroupingsHTTPException> re = exceptionResponse("Groupings Service resulted in FAILURE", gsre, 400);
        return re;
    }

    //todo Not tested
    //todo this is for the HolidayRestControllerTest test (should we really have this behavior?)
    // Is HolidayRestControllerTest even a thing anymore? Couldn't find reference to it in codebase
    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatchException(Exception ex) {
        String username = null;
        User user = userContextService.getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        }
        logger.error("username: " + username + "; Exception: ", ex);

        return "redirect:/error";
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