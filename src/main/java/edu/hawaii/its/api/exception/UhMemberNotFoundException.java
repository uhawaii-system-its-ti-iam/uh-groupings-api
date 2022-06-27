package edu.hawaii.its.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UhMemberNotFoundException extends ResponseStatusException {

    public UhMemberNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

}
