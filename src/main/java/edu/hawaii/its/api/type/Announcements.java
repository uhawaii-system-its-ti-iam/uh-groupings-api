package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

public class Announcements {
    private String resultCode;
    private List<Announcement> announcements;

    public Announcements(FindAttributesResults findAttributesResults) {
        setResultCode(findAttributesResults.getResultCode());
        setAnnouncements(findAttributesResults.getResult());
    }

    public Announcements(List<Announcement> announcements) {
        setResultCode("SUCCESS");
        setAnnouncements(announcements);
    }

    public Announcements() {
        setResultCode("FAILURE");
        setAnnouncements(new ArrayList<>());
    }

    public String getResultCode() {
        return resultCode;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    private void setAnnouncements(AttributesResult attributesResult) {
        this.announcements = JsonUtil.asList(attributesResult.getDescription(), Announcement.class);
    }

    private void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }

}
