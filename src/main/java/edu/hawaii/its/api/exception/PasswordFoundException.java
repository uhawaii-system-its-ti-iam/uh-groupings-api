package edu.hawaii.its.api.exception;

public class PasswordFoundException extends PatternFoundException {

    public PasswordFoundException(String location) {
        super(location);
    }
}
