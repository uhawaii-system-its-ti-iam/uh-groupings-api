package edu.hawaii.its.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import edu.hawaii.its.api.type.ApiSubError;

import java.util.List;

public class InvalidUhIdentifierException extends ResponseStatusException {

    private List<ApiSubError> subErrors;
    public InvalidUhIdentifierException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public InvalidUhIdentifierException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }}
