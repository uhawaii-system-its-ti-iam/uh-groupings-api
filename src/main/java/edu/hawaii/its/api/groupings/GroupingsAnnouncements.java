package edu.hawaii.its.api.groupings;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import java.util.ArrayList;
import java.util.List;

public class GroupingsAnnouncements {
    private String resultCode;
    private List<GroupingsAnnouncement> announcements;

    public GroupingsAnnouncements(FindAttributesResults findAttributesResults) {
        setAnnouncements(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());
    }

    public List<GroupingsAnnouncement> getAnnouncements() {
        return announcements;
    }

    public String getResultCode() {
        return resultCode;
    }

    private void setAnnouncements(List<AttributesResult> attributesResults) {
        JsonUtil.prettyPrint(attributesResults);
        this.announcements = new ArrayList<>();
        for (AttributesResult attributesResult : attributesResults) {
            JsonUtil.prettyPrint(attributesResult.getDescription());
            GroupingsAnnouncement groupingsAnnouncement =
                    JsonUtil.asObject(attributesResult.getDescription(), GroupingsAnnouncement.class);
            this.announcements.add(groupingsAnnouncement);
        }
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
