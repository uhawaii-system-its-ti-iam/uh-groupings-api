package edu.hawaii.its.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

@Service public class AnnouncementsService {

    @Value("${groupings.api.announcements}") private String ANNOUNCEMENTS_ATTR_NAME;

    @Value("${groupings.api.propertystring}") private String ANNOUNCEMENTS_ATTR_DEF;

    private final GrouperService grouperService;

    public AnnouncementsService(GrouperService grouperService) {
        this.grouperService = grouperService;
    }

    public Announcements getAnnouncements() {
        FindAttributesResults findAttributesResults =
                grouperService.findAttributesResults(ANNOUNCEMENTS_ATTR_DEF, ANNOUNCEMENTS_ATTR_NAME);
        Announcements allAnnouncements = new Announcements(findAttributesResults);

        // Filter to only return announcements with state 'Active'
        List<Announcement> activeAnnouncements = allAnnouncements.getAnnouncements().stream()
                .filter(announcement -> announcement.getState() == Announcement.State.Active).toList();

        return new Announcements(activeAnnouncements);
    }
}
