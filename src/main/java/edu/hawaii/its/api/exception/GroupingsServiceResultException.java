package edu.hawaii.its.api.exception;

import edu.hawaii.its.api.type.ApiSubError;
import edu.hawaii.its.api.type.GroupingsServiceResult;

import java.util.ArrayList;
import java.util.List;

public class GroupingsServiceResultException extends RuntimeException {
    /**
   * 
   */
    private List<ApiSubError> subErrors;
    private static final long serialVersionUID = 2561503964867081711L;
    private GroupingsServiceResult gsr = null;

    public GroupingsServiceResultException() {
        this.subErrors = new ArrayList<>();
    }

    public GroupingsServiceResultException(GroupingsServiceResult gsr) {
        this.gsr = gsr;
    }

    public GroupingsServiceResult getGsr() {
        return gsr;
    }

    public void setGsr(GroupingsServiceResult gsr) {
        this.gsr = gsr;
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }

    public GroupingsServiceResultException addSubError(ApiSubError subError) {
        this.subErrors.add(subError);
        return this;
    }
}
