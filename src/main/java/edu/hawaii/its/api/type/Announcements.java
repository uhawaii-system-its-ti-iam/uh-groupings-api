package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;

@Service
public class Announcements {
    private String resultCode;
    private List<Announcement> announcements = new ArrayList<>();

    public Announcements(List<Announcement> announcements) {
        if (announcements != null) {
            for (Announcement a : announcements) {
                this.announcements.add(a);
            }
            setResultCode("SUCCESS");
        }
    }

    public Announcements() {
        setResultCode("FAILURE");
    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }
    public String getResultCode() {
        return resultCode;
    }

    public List<String> validMessages(List<Announcement> allAnnouncements) {
        List<String> validMessages = new ArrayList<>();
        for (Announcement announcement : allAnnouncements) {
            //check whether each announcement message is of an Active state
            if (announcement.getState() == Announcement.State.Active) {
                validMessages.add(announcement.getMessage());
            }
        }
        return validMessages;
    }
}
