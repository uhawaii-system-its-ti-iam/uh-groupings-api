package edu.hawaii.its.api.service;

import edu.hawaii.its.api.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.ServiceTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class TestAnnouncements extends ServiceTest {

    @Autowired
    private AnnouncementsService announcementsService;
    @Autowired
    private Announcements announcements;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @BeforeEach
    void setUp() {

    }

//    @Test
//    public void getMessage() {
//        String validMessage = "second valid message"; //with the test data
//        String testMessage = announcements.getMessage();
//        assertEquals(validMessage, testMessage);
//    }

    @Test
    public void getAnnouncements() {
        Announcements test = announcementsService.oneAnnouncement();
//        logger.info("\n\nall announcements: " + test.getAnnouncements());
        JsonUtil.prettyPrint(test);

//        assertNotNull(groupingSyncDestinations);
//        assertEquals(SUCCESS, groupingSyncDestinations.getResultCode());
//        assertNotNull(groupingSyncDestinations.getSyncDestinations());
//
//
//        logger.info("\n\ninside the announcements: " + test.getAnnouncements());
//        String received = JsonUtil.prettyPrint(test.getAnnouncements());
//
//        assertEquals(theData, )

    }
}
