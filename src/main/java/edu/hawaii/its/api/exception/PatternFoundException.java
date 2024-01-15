package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.util.List;

public class PatternFoundException extends RuntimeException {

    private List<ApiSubError> subErrors;
    public PatternFoundException(String message) {
        super(message);
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public PatternFoundException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}