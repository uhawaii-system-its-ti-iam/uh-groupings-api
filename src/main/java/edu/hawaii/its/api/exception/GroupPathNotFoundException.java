package edu.hawaii.its.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import edu.hawaii.its.api.type.ApiSubError;

import java.util.ArrayList;
import java.util.List;

public class GroupPathNotFoundException extends ResponseStatusException {

    private final List<ApiSubError> subErrors;
    public GroupPathNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
        this.subErrors = new ArrayList<>();
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public GroupPathNotFoundException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }

}