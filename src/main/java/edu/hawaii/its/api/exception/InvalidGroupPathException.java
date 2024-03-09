package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class InvalidGroupPathException extends ResponseStatusException {

    private List<ApiSubError> subErrors;
    public InvalidGroupPathException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public InvalidGroupPathException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
