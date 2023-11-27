package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.hawaii.its.api.wrapper.AttributeAssignValueResult;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * GroupingTimestampResult contains results of a groupings timestamp being updated.
 */
@JsonIgnoreProperties
public class GroupingTimestampResults implements GroupingResults {

    private List<Boolean> timeUpdatedList;
    private List<String> previousUpdatedTimes;
    private List<String> currentUpdatedTimes;
    private List<String> groupPaths;
    private List<String> resultMessages;
    private String resultCode;

    public GroupingTimestampResults(UpdatedTimestampResults updatedTimestampResults) {
        Objects.requireNonNull(updatedTimestampResults, "updateTimestampResult should not be null");
        previousUpdatedTimes = updatedTimestampResults.getPreviousTimestampResults().stream()
                .map(AttributeAssignValueResult::getValue).collect(Collectors.toList());
        currentUpdatedTimes =
                updatedTimestampResults.getCurrentTimestampResults().stream().map(AttributeAssignValueResult::getValue)
                        .collect(Collectors.toList());
        timeUpdatedList = updatedTimestampResults.isTimeUpdatedList();
        groupPaths = updatedTimestampResults.getGroups().stream().map(Group::getGroupPath).collect(Collectors.toList());
        resultCode = updatedTimestampResults.getResultCode();
        setResultMessages();
    }

    public GroupingTimestampResults() {
        timeUpdatedList = Collections.emptyList();
        previousUpdatedTimes = Collections.emptyList();
        currentUpdatedTimes = Collections.emptyList();
        groupPaths = Collections.emptyList();
        resultMessages = Collections.emptyList();
        resultCode = "";
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public List<String> getGroupPaths() {
        return groupPaths;
    }

    public List<Boolean> isTimeUpdatedList() {
        return timeUpdatedList;
    }

    public void setResultMessages() {
        resultMessages = new ArrayList<>();
        for (int i = 0; i < groupPaths.size(); i++) {
            resultMessages.add(timeUpdatedList.get(i) ?
                    "Timestamp was updated from " + previousUpdatedTimes.get(i) + " to " + currentUpdatedTimes.get(i)
                            + "." :
                    "Timestamp of " + previousUpdatedTimes.get(i) + " was not updated.");
        }
    }

    public List<String> getResultMessages() {
        return resultMessages;
    }

    public List<String> getCurrentUpdatedTimeList() {
        return currentUpdatedTimes;
    }

    public List<String> getPreviousUpdatedTimeList() {
        return previousUpdatedTimes;
    }
}
