package edu.hawaii.its.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IllegalGroupingPathException extends ResponseStatusException {

    public IllegalGroupingPathException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }

}
