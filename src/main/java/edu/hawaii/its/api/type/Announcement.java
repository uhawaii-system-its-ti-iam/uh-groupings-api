package edu.hawaii.its.api.type;

import edu.hawaii.its.api.util.Dates;

import java.time.LocalDateTime;
import java.util.List;

public class Announcement {
    private String message;
    private String start;
    private String end;

//    private String name;
//    private String description;
//    private Boolean synced;

    public static String validMessage(List<Announcement> allAnnouncements) {
        LocalDateTime currDate = LocalDateTime.now();
        for (int i = 0; i < allAnnouncements.size(); i++) {
            LocalDateTime dateTo = allAnnouncements.get(i).getEnd();
            LocalDateTime dateFrom = allAnnouncements.get(i).getStart();
            if (dateFrom.isBefore(currDate) && dateTo.isAfter(currDate)) {
                return allAnnouncements.get(i).getMessage();
            }
        }
        return "";
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getStart() {
        LocalDateTime dateStart = Dates.toLocalDateTime(start, Dates.DATE_FORMAT_PLANNEDOUTAGE);
        return dateStart;
    }

    public LocalDateTime getEnd() {
        LocalDateTime dateEnd = Dates.toLocalDateTime(end, Dates.DATE_FORMAT_PLANNEDOUTAGE);
        return dateEnd;
    }

//    public void setName(String name) {  this.name = name;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String description) {
//        this.description = description;
//    }
//    public void setSynced(Boolean synced) {
//        this.synced = synced;
//    }


    public void setMessage(String message) {
        this.message = message;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
