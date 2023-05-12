package edu.hawaii.its.api.groupings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResult;

import java.util.Objects;

/**
 * GroupingTimestampResult contains results of a groupings timestamp being updated.
 */
@JsonIgnoreProperties
public class GroupingTimestampResult implements GroupingResult {

    private final boolean timeUpdated;
    private final String previousUpdatedTime;
    private final String currentUpdatedTime;
    private String resultMessage;
    private final String resultCode;
    private final String groupPath;

    public GroupingTimestampResult(UpdatedTimestampResult updatedTimestampResult) {
        Objects.requireNonNull(updatedTimestampResult, "updateTimestampResult should not be null");
        previousUpdatedTime = updatedTimestampResult.getPreviousTimestampResult().getValue();
        currentUpdatedTime = updatedTimestampResult.getCurrentTimestampResult().getValue();
        timeUpdated = updatedTimestampResult.isTimeUpdated();
        resultCode = updatedTimestampResult.getResultCode();
        groupPath = updatedTimestampResult.getGroup().getGroupPath();
        setResultMessage();
    }

    public GroupingTimestampResult() {
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
