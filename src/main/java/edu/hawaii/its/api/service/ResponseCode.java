package edu.hawaii.its.api.service;

public final class ResponseCode {

    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";
    public static final String SUCCESS_WASNT_IMMEDIATE = "SUCCESS_WASNT_IMMEDIATE";

    // Private constructor to prevent instantiation.
    private ResponseCode() {
        // Empty.
    }

}
