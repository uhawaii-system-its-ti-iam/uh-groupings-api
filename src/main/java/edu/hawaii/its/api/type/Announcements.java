package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

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

    public List<String> validMessages(List<Announcement> allGroupingsAnnouncements) {
        List<String> validMessages = new ArrayList<>();
        for (Announcement groupingsAnnouncement : allGroupingsAnnouncements) {
            //check whether each announcement message is of an Active state
            if (groupingsAnnouncement.getState() == Announcement.State.Active) {
                validMessages.add(groupingsAnnouncement.getMessage());
            }
        }
        return validMessages;
    }
}
