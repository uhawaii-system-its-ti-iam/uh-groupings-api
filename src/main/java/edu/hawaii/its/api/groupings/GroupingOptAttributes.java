package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;

/**
 * GroupingOptAttributes is used to hydrate the opt attribute toggle switches of a grouping.
 */
public class GroupingOptAttributes implements GroupingResult {
    private String resultCode;
    private String groupPath;
    private boolean optInOn;
    private boolean optOutOn;

    public GroupingOptAttributes(GroupAttributeResults groupAttributeResults) {
        setGroupPath(groupAttributeResults.getGroups());
        setResultCode(groupPath.equals("") ? "FAILURE" : "SUCCESS");
        setOptInOn(groupAttributeResults.isOptInOn());
        setOptOutOn(groupAttributeResults.isOptOutOn());
    }

    public GroupingOptAttributes() {
        setGroupPath(new ArrayList<>());
        setResultCode("FAILURE");
        setOptInOn(false);
        setOptOutOn(false);
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public boolean isOptInOn() {
        return optInOn;
    }

    public boolean isOptOutOn() {
        return optOutOn;
    }

    public void setOptInOn(boolean optInOn) {
        this.optInOn = optInOn;
    }

    public void setOptOutOn(boolean optOutOn) {
        this.optOutOn = optOutOn;
    }

    public void setGroupPath(List<Group> groups) {
        if (groups.isEmpty()) {
            this.groupPath = "";
        } else {
            this.groupPath = groups.get(0).getGroupPath();
        }
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
