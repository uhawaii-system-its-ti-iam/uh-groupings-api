package edu.hawaii.its.api.type;

import edu.hawaii.its.api.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementsTest {
    private Announcements emptyAnnouncements;
    private Announcements announcements;
    private List<Announcement> announcementsList;
    @Value("${groupings.api.announcements}")
    private String ANNOUNCEMENTS_ATTR_NAME;
    @Value("${groupings.api.propertystring}")
    private String ANNOUNCEMENTS_ATTR_DEF;
    @BeforeEach
    void setUp() {
        emptyAnnouncements = new Announcements();
        announcementsList = new ArrayList<>();

        LocalDateTime start1 = LocalDateTime.parse("2023-06-07T00:00");
        LocalDateTime end1 = LocalDateTime.parse("2023-06-15T00:00");
        LocalDateTime start2 = LocalDateTime.parse("2023-09-28T00:00");
        LocalDateTime end2 = LocalDateTime.parse("2023-11-08T00:00");
        LocalDateTime start3 = LocalDateTime.parse("2023-09-28T00:00");
        LocalDateTime end3 = LocalDateTime.parse("2023-12-15T00:00");

        announcementsList.add(new Announcement("old message", start1, end1));
        announcementsList.add(new Announcement("second old message", start2, end2));
        announcementsList.add(new Announcement("first valid message", start3, end3));
        announcements = new Announcements(announcementsList);
    }

    @Test
    void accessors() {
        //constructed with no param - empty Announcements obj
        assertNotNull(emptyAnnouncements);
        assertEquals(new ArrayList<>(), emptyAnnouncements.getAnnouncements());
        assertEquals("FAILURE", emptyAnnouncements.getResultCode());

        //construct with param - valid Announcements obj
        assertNotNull(announcements);
        assertEquals(announcementsList, announcements.getAnnouncements());
        assertEquals("SUCCESS", announcements.getResultCode());
    }

    @Test
    void validMessages() {
        //returns an empty List<String> obj when constructed empty
        assertNotNull(announcements.validMessages(announcementsList));

        //returns one valid message
        List<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("first valid message");
        assertEquals(expectedMessages, announcements.validMessages(announcementsList));

        //returns all valid messages
        LocalDateTime start4 = LocalDateTime.parse("2023-01-15T00:00");
        LocalDateTime end4 = LocalDateTime.parse("2024-01-25T00:00");
        announcementsList.add(new Announcement("second valid message", start4, end4));
        expectedMessages.add("second valid message");
        assertEquals(expectedMessages, announcements.validMessages(announcementsList));
    }
}