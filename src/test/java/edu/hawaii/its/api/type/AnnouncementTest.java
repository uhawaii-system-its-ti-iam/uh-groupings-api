package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnouncementTest {
    private Announcement expiredAnnouncement;
    private Announcement activeAnnouncement;
    private Announcement futureAnnouncement;

    @BeforeEach
    void setUp() {
        // Expired - start and end date is before the current local date time.
        LocalDateTime start1 = LocalDateTime.parse("2023-06-07T00:00");
        LocalDateTime end1 = LocalDateTime.parse("2023-06-15T00:00");
        expiredAnnouncement = new Announcement("expired message", start1, end1);
        // Valid - start date is before the current local date time and end date is after the current local date time.
        LocalDateTime start2 = LocalDateTime.parse("2023-11-07T00:00");
        LocalDateTime end2 = LocalDateTime.parse("2023-12-25T00:00");
        activeAnnouncement = new Announcement("valid message", start2, end2);
        // Future - start and date is after the current local date time.
        LocalDateTime start3 = LocalDateTime.parse("2023-12-30T00:00");
        LocalDateTime end3 = LocalDateTime.parse("2024-01-25T00:00");
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
        assertEquals(LocalDateTime.parse("2023-12-30T00:00"), futureAnnouncement.getStart());

        assertEquals(LocalDateTime.parse("2023-06-15T00:00"), expiredAnnouncement.getEnd());
        assertEquals(LocalDateTime.parse("2023-12-25T00:00"), activeAnnouncement.getEnd());
        assertEquals(LocalDateTime.parse("2024-01-25T00:00"), futureAnnouncement.getEnd());

        assertEquals(Announcement.State.Expired, expiredAnnouncement.getState());
        assertEquals(Announcement.State.Active, activeAnnouncement.getState());
        assertEquals(Announcement.State.Future, futureAnnouncement.getState());
    }
}
