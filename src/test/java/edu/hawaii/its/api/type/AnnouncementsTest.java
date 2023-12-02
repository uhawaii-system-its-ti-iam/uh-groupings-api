package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
class AnnouncementsTest {
    private Announcements emptyAnnouncements;
    private Announcements announcements;
    private List<Announcement> announcementsList;

    @BeforeEach
    void setUp() {
        emptyAnnouncements = new Announcements();
        announcementsList = new ArrayList<>();

        LocalDateTime start1 = LocalDateTime.parse("2023-06-07T00:00");
        LocalDateTime end1 = LocalDateTime.parse("2023-06-15T00:00");
        LocalDateTime start2 = LocalDateTime.parse("2023-12-06T00:00");
        LocalDateTime end2 = LocalDateTime.parse("2023-12-08T00:00");
        LocalDateTime start3 = LocalDateTime.parse("2023-12-08T00:00");
        LocalDateTime end3 = LocalDateTime.parse("2024-02-15T00:00");

        announcementsList.add(new Announcement("old message", start1, end1));
        announcementsList.add(new Announcement("Test will be down for migration to new VMs featuring Java 17 (required for Spring Boot 3)", start2, end2));
        announcementsList.add(new Announcement("Test is now running on VMs featuring Java 17 (hello Spring Boot3)", start3, end3));
        announcements = new Announcements(announcementsList);
    }

    @Test
    void accessors() {
        // Constructed with no parameters, an empty and invalid Announcements object.
        assertNotNull(emptyAnnouncements);
        assertEquals(new ArrayList<>(), emptyAnnouncements.getAnnouncements());
        assertEquals("FAILURE", emptyAnnouncements.getResultCode());

        // Constructed with parameters, a valid Announcements object.
        assertNotNull(announcements);
        assertEquals(announcementsList, announcements.getAnnouncements());
        assertEquals("SUCCESS", announcements.getResultCode());
    }

    @Test
    void validMessages() {
        // Returns a List<String> object when constructed empty.
        assertNotNull(announcements.validMessages(announcementsList));

        // Returns one valid message.
        List<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("Test is now running on VMs featuring Java 17 (hello Spring Boot3)");
        assertEquals(expectedMessages, announcements.validMessages(announcementsList));

        // Returns all valid messages.
        LocalDateTime start4 = LocalDateTime.parse("2023-01-15T00:00");
        LocalDateTime end4 = LocalDateTime.parse("2024-01-25T00:00");
        announcementsList.add(new Announcement("second valid message", start4, end4));
        expectedMessages.add("second valid message");
        assertEquals(expectedMessages, announcements.validMessages(announcementsList));
    }
}
