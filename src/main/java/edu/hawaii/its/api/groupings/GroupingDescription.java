package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.Group;

/**
 * GroupingDescription hydrates the description field in a grouping.
 */
public class GroupingDescription implements GroupingResult {

    private String groupPath;
    private String description;
    private String resultCode;

    public GroupingDescription(Group group) {
        setGroupPath(group.getGroupPath());
        setDescription(group.getDescription());
        setResultCode(group.getResultCode());
    }

    public GroupingDescription() {
        setGroupPath("");
        setDescription("");
        setResultCode("FAILURE");
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public String getDescription() {
        return description;
    }

    private void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
