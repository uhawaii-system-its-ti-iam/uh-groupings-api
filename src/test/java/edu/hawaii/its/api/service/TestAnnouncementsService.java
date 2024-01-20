package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Announcements;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAnnouncementsService {
    @Value("${groupings.api.success}")
    private String SUCCESS;
    @Autowired
    private AnnouncementsService announcementsService;

    @Test
    public void getAnnouncementsTest() {
        Announcements announcements = announcementsService.getAnnouncements();
        assertNotNull(announcements);
        assertNotNull(announcements.getAnnouncements());
        assertEquals(SUCCESS, announcements.getResultCode());
    }

}
