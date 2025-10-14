package edu.hawaii.its.api.exception;

public class DirectOwnerRemovedException extends RuntimeException {

    public DirectOwnerRemovedException() {
        super("At least one direct owner is required.");
    }
}
