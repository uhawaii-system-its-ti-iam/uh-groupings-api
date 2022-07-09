package edu.hawaii.its.api.exception;

public class AddMemberRequestRejectedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AddMemberRequestRejectedException(String message) {
        super(message);
    }

    public AddMemberRequestRejectedException(Throwable cause) {
        super(cause);
    }

    public AddMemberRequestRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getCause() {
        return super.getCause().getCause();
    }
}
