package edu.hawaii.its.api.groupings;

public abstract class GroupingsMembersResults implements GroupingsResult {
    private String resultCode;
    private String groupPath;

    protected void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    protected void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public String getResultCode() {
        return resultCode;
    }

}
