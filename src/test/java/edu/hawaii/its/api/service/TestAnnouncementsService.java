package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

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
        Announcements results = announcementsService.setAnnouncements(findAttributesResults.getResult());
        assertNotNull(results);
        assertNotNull(results.getAnnouncements());
        assertEquals(SUCCESS, results.getResultCode());
    }

    @Test
    public void activeAnnouncements() {
        List<String> results = announcementsService.activeAnnouncements();
        // Only when Grouper has this case specific data (will need to keep updating).
        assertNotNull(results);
        assertEquals("Test is now running on VMs featuring Java 17 (hello Spring Boot3)", results.get(0));
    }
}
