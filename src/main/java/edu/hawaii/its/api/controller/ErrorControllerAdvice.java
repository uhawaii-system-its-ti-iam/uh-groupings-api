package edu.hawaii.its.api.controller;

import java.io.IOException;

import jakarta.mail.MessagingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.CommandException;
import edu.hawaii.its.api.exception.GroupingsHTTPException;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.service.EmailService;
import edu.hawaii.its.api.type.ApiError;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorControllerAdvice {
    private final EmailService emailService;

    public ErrorControllerAdvice(EmailService emailService) {
        this.emailService = emailService;
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ade) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(ade, "Access Denied Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.FORBIDDEN)
                .message("Access Denied Exception")
                .stackTrace(ExceptionUtils.getStackTrace(ade))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI()); // Get the URI of the current HTTP Request

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException iae) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(iae, "Illegal Argument Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Illegal Argument Exception")
                .stackTrace(ExceptionUtils.getStackTrace(iae))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(GcWebServiceError.class)
    public ResponseEntity<ApiError> handleGcWebServiceError(GcWebServiceError gce) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(gce, "Gc Web Service Error");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Gc Web Service Error")
                .stackTrace(ExceptionUtils.getStackTrace(gce))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException hrmnse) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(hrmnse, "Http Request Method Not Supported Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .message("Http Request Method Not Supported Exception")
                .stackTrace(ExceptionUtils.getStackTrace(hrmnse))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ApiError> handleException(Exception e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(e, "Runtime Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Runtime Exception")
                .stackTrace(ExceptionUtils.getStackTrace(e))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({MessagingException.class, IOException.class})
    public ResponseEntity<ApiError> handleMessagingException(Exception me) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(me, "Messaging Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Mail service exception")
                .stackTrace(ExceptionUtils.getStackTrace(me))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleUnsupportedOperationException(UnsupportedOperationException uoe) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(uoe, "Unsupported Operation Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_IMPLEMENTED)
                .message("Unsupported Operation Exception")
                .stackTrace(ExceptionUtils.getStackTrace(uoe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UhMemberNotFoundException.class)
    protected ResponseEntity<ApiError> handleUhMemberNotFound(UhMemberNotFoundException mnfe) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(mnfe, "Uh Member Not Found Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("UH Member found failed")
                .stackTrace(ExceptionUtils.getStackTrace(mnfe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(CommandException.class)
    public ResponseEntity<ApiError> handleCommandException(CommandException ce, WebRequest request) {
        String path = request.getDescription(false);

        emailService.sendWithStack(ce, "Command Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_ACCEPTABLE)
                .message("Command Exception")
                .stackTrace(ExceptionUtils.getStackTrace(ce))
                .resultCode("FAILURE")
                .path(path);

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(GroupingsHTTPException.class)
    public ResponseEntity<ApiError> handleGroupingsHTTPException(GroupingsHTTPException ghe) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(ghe, "Groupings HTTP Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.FORBIDDEN)
                .message("Groupings HTTP Exception")
                .stackTrace(ExceptionUtils.getStackTrace(ghe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(InvalidGroupPathException.class)
    public ResponseEntity<ApiError> handleInvalidGroupPathException(InvalidGroupPathException igpe) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        emailService.sendWithStack(igpe, "Invalid Group Path Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Invalid Group Path Exception")
                .stackTrace(ExceptionUtils.getStackTrace(igpe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
}
