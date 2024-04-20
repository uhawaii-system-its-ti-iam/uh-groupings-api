package edu.hawaii.its.api.type;

import edu.hawaii.its.api.wrapper.Subject;

public class GroupingsServiceResult {
    private String action = "null";
    private String resultCode = "null";
    private Subject subject = null;

    // Constructor
    public GroupingsServiceResult() {
    }

    // Constructor
    public GroupingsServiceResult(String resultCode, String action) {
        this.resultCode = resultCode;
        this.action = action;
    }

    // Constructor
    public GroupingsServiceResult(String resultCode, String action, Subject subject) {
        this.resultCode = resultCode;
        this.action = action;
        this.subject = subject;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override public String toString() {
        return "GroupingsServiceResult{" + "action='" + action + '\'' + ", resultCode='" + resultCode + '\''
                + ", subject=" + subject + '}';
    }
}
