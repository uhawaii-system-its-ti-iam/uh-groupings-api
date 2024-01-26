package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnouncementsTest {
    private Announcements announcements;
    private Announcements emptyAnnouncements;
    private List<Announcement> announcementList;
    private LocalDateTime currentDateTime = LocalDateTime.now();

    @BeforeEach
    public void setup() {
        emptyAnnouncements = new Announcements();

        LocalDateTime start1 = LocalDateTime.parse("2023-06-07T00:00");
        LocalDateTime end1 = LocalDateTime.parse("2023-06-15T00:00");
        LocalDateTime start2 = currentDateTime.plusDays(-5);
        LocalDateTime end2 = currentDateTime.plusDays(5);
        LocalDateTime start3 = currentDateTime.plusDays(5);
        LocalDateTime end3 = currentDateTime.plusDays(10);

        announcementList = new ArrayList<>();
        announcementList.add(new Announcement("old message", start1, end1));
        announcementList.add(new Announcement("Test will be down for migration to new VMs featuring Java 17 (required for Spring Boot 3)", start2, end2));
        announcementList.add(new Announcement("Test is now running on VMs featuring Java 17 (hello Spring Boot3)", start3, end3));
        announcements = new Announcements(announcementList);
    }

    @Test
    public void accessors() {
        // Constructed with no parameters, an empty and invalid Announcements object.
        assertNotNull(emptyAnnouncements);
        assertEquals(new ArrayList<>(), emptyAnnouncements.getAnnouncements());
        assertEquals("FAILURE", emptyAnnouncements.getResultCode());

        // Constructed with parameters, a valid Announcements object.
        assertNotNull(announcements);
        assertEquals(announcementList, announcements.getAnnouncements());
        assertEquals("SUCCESS", announcements.getResultCode());
    }

}
