package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.wrapper.GroupSaveResults;

import java.util.Objects;

public class GroupingsUpdateDescriptionResult implements GroupingsResult {
    private final GroupSaveResults groupSaveResults;

    private final String updatedDescription;

    private String currentDescription;

    private String resultCode;

    private String groupPath;

    public GroupingsUpdateDescriptionResult(GroupSaveResults groupSaveResults, String description) {
        Objects.requireNonNull(groupSaveResults);
        this.groupSaveResults = groupSaveResults;
        this.updatedDescription = description;
        setResultCode();
        setGroupPath();
        setCurrentDescription();
    }

    public GroupingsUpdateDescriptionResult() {
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
