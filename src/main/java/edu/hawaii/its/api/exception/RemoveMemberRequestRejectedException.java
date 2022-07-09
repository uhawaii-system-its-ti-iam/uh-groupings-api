package edu.hawaii.its.api.exception;

public class RemoveMemberRequestRejectedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RemoveMemberRequestRejectedException(String message) {
        super(message);
    }

    public RemoveMemberRequestRejectedException(Throwable cause) {
        super(cause);
    }

    public RemoveMemberRequestRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getCause() {
        return super.getCause().getCause();
    }
}
