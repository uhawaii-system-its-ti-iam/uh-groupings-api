package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Feedback {

    @JsonIgnoreProperties
    private String name = "";

    private String email;
    private String type;
    private String message;
    
    @JsonIgnoreProperties
    private String exceptionMessage = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public String toString() {
        return "Feedback [email=" + email
                + ", message=" + message
                + ", name=" + name
                + ", type=" + type
                + ", exceptionMessage=" + exceptionMessage
                + "]";
    }

}

