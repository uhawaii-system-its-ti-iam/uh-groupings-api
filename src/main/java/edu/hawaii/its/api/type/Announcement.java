package edu.hawaii.its.api.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import edu.hawaii.its.api.util.Dates;

import java.time.LocalDateTime;
import java.util.List;

public class Announcement {
    private String message;
    private String from;
    private String to;

    public static String validMessage(List<Announcement> allAnnouncements) {
        LocalDateTime currDate = LocalDateTime.now();
        for (int i = 0; i < allAnnouncements.size(); i++) {
            LocalDateTime dateTo = allAnnouncements.get(i).getTo();
            LocalDateTime dateFrom = allAnnouncements.get(i).getFrom();
            if (dateFrom.isBefore(currDate) && dateTo.isAfter(currDate)) {
                return allAnnouncements.get(i).getMessage();
            }
        }
        return "";
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getFrom() {
        LocalDateTime dateFrom = Dates.toLocalDateTime(from, Dates.DATE_FORMAT_PLANNEDOUTAGE);
        return dateFrom;
    }

    public LocalDateTime getTo() {
        LocalDateTime dateTo = Dates.toLocalDateTime(to, Dates.DATE_FORMAT_PLANNEDOUTAGE);
        return dateTo;
    }


    //not used
    public void setMessage(String message) {
        this.message = message;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
