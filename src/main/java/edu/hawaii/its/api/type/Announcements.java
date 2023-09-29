package edu.hawaii.its.api.type;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import java.util.List;

public class Announcements {
    private String resultCode;
    private List<Announcement> announcements;

    public Announcements(FindAttributesResults findAttributesResults) {
        setAnnouncements(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());
    }

    public Announcements() {
        System.out.println("empty param constructor for announcements");
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public String getResultCode() {
        return resultCode;
    }

    private void setAnnouncements(List<AttributesResult> attributesResults) {
        AttributesResult attributesResult = attributesResults.get(0);
        this.announcements = JsonUtil.asObjectList(attributesResult.getDescription(), Announcement.class);
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
