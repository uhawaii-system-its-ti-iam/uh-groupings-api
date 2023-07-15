package edu.hawaii.its.api.type;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        findAnnouncements(new ArrayList<>()); //need to have, cannot change to set?
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

    private void findAnnouncements(List<AttributesResult> attributesResults) {
        this.announcements = new ArrayList<>();
        //hydrate this.announcements by creating Announcement objects that grab from the Ws
        for (AttributesResult attributesResult : attributesResults) {
            Announcement announcement =
                    JsonUtil.asObject(attributesResult.getDescription(), Announcement.class);
            this.announcements.add(announcement);
        }
    }
}
