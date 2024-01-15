package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.util.List;

public class AccessDeniedException extends RuntimeException {

    private List<ApiSubError> subErrors;
    private static final long serialVersionUID = 1L;

    public AccessDeniedException() {
        super("Insufficient Privileges");
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public AccessDeniedException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
