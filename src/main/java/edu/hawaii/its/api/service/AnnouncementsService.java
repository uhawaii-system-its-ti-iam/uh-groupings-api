package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementsService {
    public static final Log log = LogFactory.getLog(AnnouncementsService.class);
    @Value("${groupings.api.announcements}")
    private String ANNOUNCEMENTS_ATTR_NAME;
    @Value("${groupings.api.propertystring}")
    private String ANNOUNCEMENTS_ATTR_DEF;
    @Autowired private GrouperApiService grouperApiService;

    public Announcements setAnnouncements(List<AttributesResult> attributesResults) {
        //attributesResults[0] holds the whole response -> multiple announcements
        AttributesResult attributesResult = attributesResults.get(0);
        //hydrate announcements by creating a list of Announcement.class objects
        List<Announcement> announcementsList = new ArrayList<>();
        announcementsList = JsonUtil.asObjectList(attributesResult.getDescription(), Announcement.class); //seems to be initializing announcement
        System.out.println(announcementsList);
        return new Announcements(announcementsList);
    }

    public List<String> allAnnouncements() {
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                ANNOUNCEMENTS_ATTR_DEF,
                ANNOUNCEMENTS_ATTR_NAME);
        Announcements allAnnouncements = setAnnouncements(findAttributesResults.getResults());
        return allAnnouncements.validMessages(allAnnouncements.getAnnouncements());
    }
}
