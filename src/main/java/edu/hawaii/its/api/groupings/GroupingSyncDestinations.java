package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * GroupingSyncDestinations contains the necessary data hydrate the sync destinations tab in a grouping.
 */
public class GroupingSyncDestinations {
    private String resultCode;

    private List<GroupingSyncDestination> syncDestinations;

    private List<GroupAttribute> groupAttributes;

    private String groupExtension;

    private final KeyParser keyParser;

    public GroupingSyncDestinations(FindAttributesResults findAttributesResults,
            GroupAttributeResults groupAttributeResults, KeyParser keyParser) {
        this.keyParser = keyParser;
        setGroupExtension(groupAttributeResults.getGroups());
        setGroupAttributes(groupAttributeResults.getGroupAttributes());
        setSyncDestination(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());
    }

    public GroupingSyncDestinations() {
        this.keyParser = new KeyParser("", Pattern.compile(""));
        setGroupExtension(new ArrayList<>());
        setGroupAttributes(new ArrayList<>());
        setSyncDestination(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public String getResultCode() {
        return resultCode;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<GroupingSyncDestination> getSyncDestinations() {
        return syncDestinations;
    }

    public String getGroupExtension() {
        return groupExtension;
    }

    private void setGroupExtension(List<Group> groups) {
        this.groupExtension = !groups.isEmpty() ? groups.get(0).getExtension() : "";
    }

    public GroupingSyncDestination getGoogleGroup() {
        for (GroupingSyncDestination groupingSyncDestination : syncDestinations) {
            if (groupingSyncDestination.getName().contains("google-group")) {
                return groupingSyncDestination;
            }
        }
        return null;
    }

    private void setSyncDestination(List<AttributesResult> attributesResults) {
        this.syncDestinations = new ArrayList<>();
        for (AttributesResult attributesResult : attributesResults) {
            GroupingSyncDestination groupingSyncDestination =
                    JsonUtil.asObject(attributesResult.getDescription(), GroupingSyncDestination.class);
            groupingSyncDestination.setName(attributesResult.getName());
            groupingSyncDestination.setSynced(this.groupAttributes.stream()
                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
            this.syncDestinations.add(groupingSyncDestination);
        }
        keyParser.replaceRegex(this.syncDestinations, groupExtension);
        this.syncDestinations.sort(Comparator.comparing(GroupingSyncDestination::getDescription));
    }

    private void setGroupAttributes(List<GroupAttribute> groupAttributes) {
        this.groupAttributes = groupAttributes;
    }
}
