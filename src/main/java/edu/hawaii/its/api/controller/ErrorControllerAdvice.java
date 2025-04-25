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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.service.EmailService;
import edu.hawaii.its.api.type.ApiError;

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
    
    public String extractEndpoint(WebRequest request) {
        if (request == null) {
            return null;
        }
        
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        
        String description = request.getDescription(false);
        return (description != null && description.startsWith("uri="))
                ? description.substring(4)
                : null;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ade, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(ade, "Access Denied Exception", path);
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
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException iae, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(iae, "Illegal Argument Exception", path);
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Illegal Argument Exception")
                .stackTrace(ExceptionUtils.getStackTrace(iae))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException hrmnse, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(hrmnse, "Http Request Method Not Supported Exception", path);
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
    public ResponseEntity<ApiError> handleException(Exception e, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(e, "Runtime Exception", path);
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
    public ResponseEntity<ApiError> handleMessagingException(Exception me, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(me, "Messaging Exception", path);
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
    public ResponseEntity<ApiError> handleUnsupportedOperationException(UnsupportedOperationException uoe,
            WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(uoe, "Unsupported Operation Exception", path);
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_IMPLEMENTED)
                .message("Unsupported Operation Exception")
                .stackTrace(ExceptionUtils.getStackTrace(uoe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UhIdentifierNotFoundException.class)
    protected ResponseEntity<ApiError> handleUhMemberNotFound(UhIdentifierNotFoundException mnfe, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(mnfe, "Uh Member Not Found Exception", path);
        ApiError.Builder errorBuilder = new ApiError.Builder()
                .status(HttpStatus.NOT_FOUND)
                .message("UH Member found failed")
                .stackTrace(ExceptionUtils.getStackTrace(mnfe))
                .resultCode("FAILURE")
                .path(attributes.getRequest().getRequestURI());

        ApiError apiError = errorBuilder.build();

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(InvalidGroupPathException.class)
    public ResponseEntity<ApiError> handleInvalidGroupPathException(InvalidGroupPathException igpe,
            WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String path = extractEndpoint(request);
        
        emailService.sendWithStack(igpe, "Invalid Group Path Exception", path);
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
