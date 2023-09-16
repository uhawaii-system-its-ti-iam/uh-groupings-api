package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    @Autowired
    private GrouperApiService grouperApiService;

    public Announcements getAnnouncements() {
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                "uh-settings:attributes:for-applications:uhgroupings:propertyString",
                "uh-settings:attributes:for-applications:uhgroupings"
        );
        return new Announcements(findAttributesResults);
    }
    
}
