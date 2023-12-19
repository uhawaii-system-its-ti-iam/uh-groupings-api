package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.util.ArrayList;
import java.util.List;

public class ExceptionForTesting extends RuntimeException {

    private List<ApiSubError> subErrors;

    public ExceptionForTesting(String message) {
        super(message);
        this.subErrors = new ArrayList<>();
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public ExceptionForTesting addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
