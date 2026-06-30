package edu.hawaii.its.api.type;

import java.time.Clock;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

public class Announcement {
    private final String message;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyyMMdd'T'HHmmss")
    private final LocalDateTime start;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyyMMdd'T'HHmmss")
    private final LocalDateTime end;

    public enum State {
        Active,
        Expired,
        Future
    }

    @JsonCreator
    Announcement(@JsonProperty("message") String message, @JsonProperty("start") LocalDateTime start,
            @JsonProperty("end") LocalDateTime end) {
        this.message = message;
        this.start = start;
        this.end = end;
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

    @JsonIgnore
    public State getState() {
        return getState(Clock.systemDefaultZone());
    }

    @JsonIgnore
    public State getState(Clock clock) {
        LocalDateTime currDate = LocalDateTime.now(clock);
        if (start.isBefore(currDate) && end.isAfter(currDate)) {
            return State.Active;
        } else if (start.isAfter(currDate)) {
            return State.Future;
        } else {
            return State.Expired;
        }
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
