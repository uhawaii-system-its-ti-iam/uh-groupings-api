package edu.hawaii.its.api.groupings;

import java.util.Objects;

import edu.hawaii.its.api.wrapper.GroupSaveResults;

/**
 * GroupingUpdateDescriptionResult contains results of an updated description.
 */
public class GroupingUpdateDescriptionResult implements GroupingResult {
    private final GroupSaveResults groupSaveResults;

    private final String updatedDescription;

    private String currentDescription;

    private String resultCode;

    private String groupPath;

    public GroupingUpdateDescriptionResult(GroupSaveResults groupSaveResults, String description) {
        Objects.requireNonNull(groupSaveResults);
        this.groupSaveResults = groupSaveResults;
        this.updatedDescription = description;
        setResultCode();
        setGroupPath();
        setCurrentDescription();
    }

    public GroupingUpdateDescriptionResult() {
        this.groupSaveResults = new GroupSaveResults();
        this.updatedDescription = "";
        setResultCode();
        setGroupPath();
        setCurrentDescription();
    }

    public String getUpdatedDescription() {
        return this.updatedDescription;

    }

    public String getCurrentDescription() {
        return currentDescription;
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    private void setResultCode() {
        this.resultCode = groupSaveResults.getResultCode();
    }

    private void setGroupPath() {
        this.groupPath = this.groupSaveResults.getGroup().getGroupPath();
    }

    private void setCurrentDescription() {
        this.currentDescription = this.groupSaveResults.getGroup().getDescription();
    }

}
