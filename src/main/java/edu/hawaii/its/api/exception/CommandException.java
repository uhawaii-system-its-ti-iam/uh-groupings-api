package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.util.ArrayList;
import java.util.List;

public class CommandException extends RuntimeException {
    private final List<ApiSubError> subErrors;

    public CommandException() {
        super("Command Error");
        this.subErrors = new ArrayList<>();
    }

    public CommandException(String message) {
        super(message);
        this.subErrors = new ArrayList<>();
    }

    public CommandException(Throwable cause) {
        super(cause);
        this.subErrors = new ArrayList<>();
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
        this.subErrors = new ArrayList<>();
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public CommandException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
