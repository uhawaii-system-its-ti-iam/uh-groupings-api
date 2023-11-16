package edu.hawaii.its.api.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementTest {
    private Announcement expiredAnnouncement;
    private Announcement activeAnnouncement;
    private Announcement futureAnnouncement;

    @BeforeEach
    void setUp() {
        //Expired
        LocalDateTime start1 = LocalDateTime.parse("2023-06-07T00:00");
        LocalDateTime end1 = LocalDateTime.parse("2023-06-15T00:00");
        expiredAnnouncement = new Announcement("expired message", start1, end1);
        //Valid
        LocalDateTime start2 = LocalDateTime.parse("2023-11-07T00:00");
        LocalDateTime end2 = LocalDateTime.parse("2023-11-25T00:00");
        activeAnnouncement = new Announcement("valid message", start2, end2);
        //Future
        LocalDateTime start3 = LocalDateTime.parse("2023-12-10T00:00");
        LocalDateTime end3 = LocalDateTime.parse("2023-01-25T00:00");
        futureAnnouncement = new Announcement("future message", start3, end3);
    }

    @Test
    void accessors() {
        assertNotNull(expiredAnnouncement);
        assertNotNull(activeAnnouncement);
        assertNotNull(futureAnnouncement);

        assertEquals("expired message", expiredAnnouncement.getMessage());
        assertEquals("valid message", activeAnnouncement.getMessage());
        assertEquals("future message", futureAnnouncement.getMessage());

        assertEquals(LocalDateTime.parse("2023-06-07T00:00"), expiredAnnouncement.getStart());
        assertEquals(LocalDateTime.parse("2023-11-07T00:00"), activeAnnouncement.getStart());
        assertEquals(LocalDateTime.parse("2023-12-10T00:00"), futureAnnouncement.getStart());

        assertEquals(LocalDateTime.parse("2023-06-15T00:00"), expiredAnnouncement.getEnd());
        assertEquals(LocalDateTime.parse("2023-11-25T00:00"), activeAnnouncement.getEnd());
        assertEquals(LocalDateTime.parse("2023-01-25T00:00"), futureAnnouncement.getEnd());

        assertEquals(Announcement.State.Expired, expiredAnnouncement.getState());
        assertEquals(Announcement.State.Active, activeAnnouncement.getState());
        assertEquals(Announcement.State.Future, futureAnnouncement.getState());
    }
}