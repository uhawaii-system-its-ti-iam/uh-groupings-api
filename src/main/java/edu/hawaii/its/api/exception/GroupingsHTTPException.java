package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class GroupingsHTTPException extends RuntimeException{
    /**
   * 
   */
    private List<ApiSubError> subErrors;
    private static final long serialVersionUID = -5579769846632452949L;
    private Integer statusCode = null;
    private String string = null;

    public GroupingsHTTPException() {
        //empty
    }

    public GroupingsHTTPException(String message) {
        super(message);
    }

    public GroupingsHTTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupingsHTTPException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.setStackTrace(cause.getStackTrace());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        this.string = sStackTrace;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getString() {
        return string;
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public GroupingsHTTPException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
