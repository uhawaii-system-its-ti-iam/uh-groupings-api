package edu.hawaii.its.api.type;

public class GroupingsServiceResultException extends RuntimeException {
    /**
   * 
   */
    private static final long serialVersionUID = 2561503964867081711L;
    private GroupingsServiceResult gsr = null;

    public GroupingsServiceResultException() {
        //empty
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
}
