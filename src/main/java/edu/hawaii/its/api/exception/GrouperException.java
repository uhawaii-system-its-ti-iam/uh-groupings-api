package edu.hawaii.its.api.exception;

public class GrouperException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GrouperException() {
        super("An error occurred upstream from GrouperClient");
    }

    public GrouperException(String message) {
        super(message);
    }

    public GrouperException(Throwable cause) {
        super(cause);
    }

    public GrouperException(String message, Throwable cause) {
        super(message, cause);
    }
}
