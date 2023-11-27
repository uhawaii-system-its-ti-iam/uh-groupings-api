package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

/**
 * GroupingSyncDestinations contains the necessary data hydrate the sync destinations tab in a grouping.
 */
public class GroupingSyncDestinations {
    private String resultCode;
    private List<GroupingSyncDestination> syncDestinations;
    private List<GroupAttribute> groupAttributes;
    private String groupingExtension;

    public GroupingSyncDestinations(FindAttributesResults findAttributesResults,
            GroupAttributeResults groupAttributeResults) {
        setGroupingExtension(groupAttributeResults.getGroups());
        setGroupAttributes(groupAttributeResults.getGroupAttributes());
        setSyncDestination(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());

    }

    public GroupingSyncDestinations() {
        setGroupingExtension(new ArrayList<>());
        setGroupAttributes(new ArrayList<>());
        setSyncDestination(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public String getResultCode() {
        return resultCode;
    }

    public List<GroupingSyncDestination> getSyncDestinations() {
        return syncDestinations;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setSyncDestination(List<AttributesResult> attributesResults) {
        this.syncDestinations = new ArrayList<>();
        for (AttributesResult attributesResult : attributesResults) {
            GroupingSyncDestination groupingSyncDestination =
                    JsonUtil.asObject(attributesResult.getDescription(), GroupingSyncDestination.class);
            groupingSyncDestination.setName(attributesResult.getName());
            groupingSyncDestination.setDescription(groupingSyncDestination.getDescription()
                    .replaceFirst("\\$\\{srhfgs}", this.groupingExtension));
            groupingSyncDestination.setSynced(this.groupAttributes.stream()
                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
            this.syncDestinations.add(groupingSyncDestination);
        }
        this.syncDestinations.sort(Comparator.comparing(GroupingSyncDestination::getDescription));
    }

    private void setGroupAttributes(List<GroupAttribute> groupAttributes) {
        this.groupAttributes = groupAttributes;
    }

    private void setGroupingExtension(List<Group> groups) {
        this.groupingExtension = !groups.isEmpty() ? groups.get(0).getExtension() : "";
    }
}
