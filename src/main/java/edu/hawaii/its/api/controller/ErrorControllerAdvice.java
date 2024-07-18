package edu.hawaii.its.api.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import edu.hawaii.its.api.exception.CommandException;
import edu.hawaii.its.api.exception.GroupingsHTTPException;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import jakarta.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.ExceptionForTesting;
import edu.hawaii.its.api.exception.GroupingsServiceResultException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.service.EmailService;
import edu.hawaii.its.api.type.ApiError;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorControllerAdvice {

    private static final Log logger = LogFactory.getLog(ErrorControllerAdvice.class);

    private final EmailService emailService;

    public ErrorControllerAdvice(EmailService emailService) {
        this.emailService = emailService;
    }

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ade) {
        emailService.sendWithStack(ade, "Access Denied Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.FORBIDDEN)
                .message("Access Denied Exception")
                .debugMessage("The current user does not have permission to perform this action.")
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException iae) {
        emailService.sendWithStack(iae, "Illegal Argument Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Illegal Argument Exception")
                .debugMessage("Resource not available")
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(GcWebServiceError.class)
    public ResponseEntity<ApiError> handleGcWebServiceError(GcWebServiceError gce) {
        emailService.sendWithStack(gce, "Gc Web Service Error");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Gc Web Service Error")
                .debugMessage(gce.getMessage())
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
  }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException hrmnse) {
        emailService.sendWithStack(hrmnse, "Http Request Method Not Supported Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .message("Http Request Method Not Supported Exception")
                .debugMessage(hrmnse.getMessage())
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ApiError> handleException(Exception exception) {
        emailService.sendWithStack(exception, "Runtime Exception");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Runtime Exception")
                .debugMessage(exception.getMessage())
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({MessagingException.class, IOException.class})
    public ResponseEntity<ApiError> handleMessagingException(Exception e) {
        emailService.sendWithStack(e, "Messaging Exception");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Mail service exception")
                .debugMessage(e.getMessage())
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleUnsupportedOperationException(
        UnsupportedOperationException ex) {
        emailService.sendWithStack(ex, "Unsupported Operation Exception");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_IMPLEMENTED)
                .message("Unsupported Operation Exception")
                .debugMessage(ex.getMessage())
                .timestamp(LocalDateTime.now());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(ExceptionForTesting.class)
    protected ResponseEntity<ApiError> handleExceptionForTesting(ExceptionForTesting ex) {
        emailService.sendWithStack(ex, "Exception For Testing Exception");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Validation failed")
                .debugMessage("Check your service at endpoint /exception")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(ex.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(UhMemberNotFoundException.class)
    protected ResponseEntity<ApiError> handleUhMemberNotFound(UhMemberNotFoundException ex) {
        emailService.sendWithStack(ex, "Uh Member Not Found Exception");

        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("UH Member found failed")
                .debugMessage("This is not the validation error from frontend, check Sub Error to see the reason in the backend")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(ex.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(CommandException.class)
    public ResponseEntity<ApiError> handleCommandException(CommandException ce) {
        emailService.sendWithStack(ce, "Command Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_ACCEPTABLE)
                .message("Command Exception")
                .debugMessage("There's an error from the command of grouper or grouping")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(ce.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(GroupingsHTTPException.class)
    public ResponseEntity<ApiError> handleGroupingsHTTPException(GroupingsHTTPException ghe) {
        emailService.sendWithStack(ghe, "Groupings HTTP Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.FORBIDDEN)
                .message("Groupings HTTP Exception")
                .debugMessage("The current user does not have permission to perform this action.")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(ghe.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
    @ExceptionHandler(GroupingsServiceResultException.class)
    public ResponseEntity<ApiError> handleGroupingsServiceResultException(
            GroupingsServiceResultException gsre) {
        emailService.sendWithStack(gsre, "Groupings ServiceResult Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Groupings ServiceResult Exception")
                .debugMessage("Groupings Service resulted in FAILURE.")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(gsre.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(InvalidGroupPathException.class)
    public ResponseEntity<ApiError> handleInvalidGroupPathException(
            InvalidGroupPathException ex) {
        emailService.sendWithStack(ex, "Invalid Group Path Exception");
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Invalid Group Path Exception")
                .debugMessage("The group path you are using is wrong")
                .timestamp(LocalDateTime.now());

        errorBuilder.addAllSubErrors(ex.getSubErrors());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }
}
