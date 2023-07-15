package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcements;

import edu.hawaii.its.api.wrapper.FindAttributesResults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAnnouncementsService {
    @Value("${groupings.api.announcements}")
    private String ANNOUNCEMENTS_ATTR_NAME;
    @Value("${groupings.api.propertystring}")
    private String ANNOUNCEMENTS_ATTR_DEF;
    @Value("${groupings.api.success}")
    private String SUCCESS;
    @Autowired
    private AnnouncementsService announcementsService;
    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void setAnnouncements() {
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                ANNOUNCEMENTS_ATTR_DEF,
                ANNOUNCEMENTS_ATTR_NAME);
        assertNotNull(findAttributesResults.getResults());

        Announcements results = announcementsService.setAnnouncements(findAttributesResults.getResults());
        assertNotNull(results);
        assertNotNull(results.getAnnouncements());
        assertEquals(SUCCESS, results.getResultCode());
    }

    @Test
    public void allAnnouncements() {
        List<String> results = announcementsService.allAnnouncements();

        //only when there is data - will need to keep updating
        assertNotNull(results);
        assertEquals("first valid message", results.get(0));
        assertEquals("second valid message", results.get(1));
    }
}
