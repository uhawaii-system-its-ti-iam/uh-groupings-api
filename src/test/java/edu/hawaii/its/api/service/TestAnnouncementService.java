package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.ServiceTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static edu.hawaii.its.api.service.PathFilter.logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAnnouncementService extends ServiceTest {
    @Autowired
    private AnnouncementService announcementService;

    @Test
    public void groupingsAnnouncements() {
        Announcements test = announcementService.findAnnouncements();
        logger.info("\n\nall announcements: " + test);

        logger.info("\n\ninside the announcements: " + test.getAnnouncements());
//        JsonUtil.prettyPrint(test.getAnnouncements());

    }
}
