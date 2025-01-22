package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.util.List;
import java.util.ArrayList;

public class LimitExceedException extends RuntimeException {

    private List<ApiSubError> subErrors;

    public LimitExceedException(String message) {
        super(message);
        this.subErrors = new ArrayList<>();
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public LimitExceedException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
