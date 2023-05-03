package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

import java.util.ArrayList;
import java.util.List;

/**
 * GroupingSyncDestinations contains the necessary data hydrate the sync destinations tab in a grouping.
 */
public class GroupingSyncDestinations {
    private String resultCode;
    private List<GroupingSyncDestination> syncDestinations;
    private List<GroupAttribute> groupAttributes;

    public GroupingSyncDestinations(FindAttributesResults findAttributesResults,
            GroupAttributeResults groupAttributeResults) {
        setGroupAttributes(groupAttributeResults.getGroupAttributes());
        setSyncDestination(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());

    }

    public GroupingSyncDestinations() {
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
            groupingSyncDestination.setSynced(this.groupAttributes.stream()
                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
            this.syncDestinations.add(groupingSyncDestination);
        }
    }

    private void setGroupAttributes(List<GroupAttribute> groupAttributes) {
        this.groupAttributes = groupAttributes;
    }
}
