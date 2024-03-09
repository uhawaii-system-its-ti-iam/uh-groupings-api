package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

@Service
public class AnnouncementsService {
    public static final Log log = LogFactory.getLog(AnnouncementsService.class);

    @Value("${groupings.api.announcements}")
    private String ANNOUNCEMENTS_ATTR_NAME;

    @Value("${groupings.api.propertystring}")
    private String ANNOUNCEMENTS_ATTR_DEF;

    @Autowired
    private GrouperService grouperService;

    public Announcements getAnnouncements() {
        FindAttributesResults findAttributesResults = grouperService.findAttributesResults(
                ANNOUNCEMENTS_ATTR_DEF,
                ANNOUNCEMENTS_ATTR_NAME);
        return new Announcements(findAttributesResults);
    }
}
