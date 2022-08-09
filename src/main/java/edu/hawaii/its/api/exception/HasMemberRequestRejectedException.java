package edu.hawaii.its.api.exception;

public class HasMemberRequestRejectedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HasMemberRequestRejectedException(String message) {
        super(message);
    }

    public HasMemberRequestRejectedException(Throwable cause) {
        super(cause);
    }

    public HasMemberRequestRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getCause() {
        return super.getCause().getCause();
    }
}
