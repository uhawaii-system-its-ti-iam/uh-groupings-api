package edu.hawaii.its.api.exception;

public class OwnerLimitExceededException extends RuntimeException {

    public OwnerLimitExceededException() {
        super("Exceeded limit of allowed owners for a grouping.");
    }
}