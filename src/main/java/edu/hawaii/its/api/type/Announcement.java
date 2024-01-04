package edu.hawaii.its.api.type;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;


public class Announcement {
    private String message = "";
    private State state;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyyMMdd'T'HHmmss")
    private LocalDateTime start;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyyMMdd'T'HHmmss")
    private LocalDateTime end;

    public enum State {
        Active,
        Expired,
        Future
    }

    Announcement(@JsonProperty("message") String message,
                 @JsonProperty("start") LocalDateTime start,
                 @JsonProperty("end") LocalDateTime end) {
        this.message = message;
        this.start = start;
        this.end = end;

        LocalDateTime currDate = LocalDateTime.now();
        if (start.isBefore(currDate) && end.isAfter(currDate)) {
            state = State.Active;
        } else if (start.isAfter(currDate)) {
            state = State.Future;
        } else {
            state = State.Expired;
        }
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Announcement [" +
                "message='" + message + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                "]";
    }
}
