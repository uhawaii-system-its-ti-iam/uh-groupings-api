package edu.hawaii.its.api.groupings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResult;

import java.util.Objects;

@JsonIgnoreProperties
public class GroupingsTimestampResult implements GroupingsResult {

    private final boolean timeUpdated;
    private final String previousUpdatedTime;
    private final String currentUpdatedTime;
    private String resultMessage;
    private final String resultCode;
    private final String groupPath;

    public GroupingsTimestampResult(UpdatedTimestampResult updatedTimestampResult) {
        Objects.requireNonNull(updatedTimestampResult, "updateTimestampResult should not be null");
        previousUpdatedTime = updatedTimestampResult.getPreviousTimestampResult().getValue();
        currentUpdatedTime = updatedTimestampResult.getCurrentTimestampResult().getValue();
        timeUpdated = updatedTimestampResult.isTimeUpdated();
        resultCode = updatedTimestampResult.getResultCode();
        groupPath = updatedTimestampResult.getGroup().getGroupPath();
        setResultMessage();
    }

    public GroupingsTimestampResult() {
        previousUpdatedTime = "";
        currentUpdatedTime = "";
        timeUpdated = false;
        resultCode = "";
        groupPath = "";
        resultMessage = "";
    }

    public String getResultCode() {
        return resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public boolean isTimeUpdated() {
        return timeUpdated;
    }

    public void setResultMessage() {
        resultMessage = (timeUpdated) ?
                "Timestamp was updated from " + previousUpdatedTime + " to " + currentUpdatedTime + "." :
                "Timestamp of " + previousUpdatedTime + " was not updated.";
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getCurrentUpdatedTime() {
        return currentUpdatedTime;
    }

    public String getPreviousUpdatedTime() {
        return previousUpdatedTime;
    }
}
