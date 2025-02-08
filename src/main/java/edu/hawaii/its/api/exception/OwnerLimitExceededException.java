package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;
import org.springframework.http.*;
import org.springframework.web.server.*;

import java.util.List;
import java.util.ArrayList;

public class OwnerLimitExceededException extends ResponseStatusException {

    private List<ApiSubError> subErrors;

    public OwnerLimitExceededException(String message) {
        super(HttpStatus.CONFLICT, message);
        this.subErrors = new ArrayList<>();
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public OwnerLimitExceededException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
