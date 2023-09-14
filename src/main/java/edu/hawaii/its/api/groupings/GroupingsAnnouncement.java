package edu.hawaii.its.api.groupings;

import java.time.LocalDateTime;

public class GroupingsAnnouncement {
    private String message;
    private LocalDateTime from;
    private LocalDateTime to;

    public String getMessage() {
        return message;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }
}
