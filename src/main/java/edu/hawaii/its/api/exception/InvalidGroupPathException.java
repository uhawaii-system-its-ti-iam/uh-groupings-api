package edu.hawaii.its.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidGroupPathException extends ResponseStatusException {
    public InvalidGroupPathException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

}
