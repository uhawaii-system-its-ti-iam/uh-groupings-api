package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    @Autowired
    private GrouperApiService grouperApiService;

    public Announcements getAnnouncements(String currUser) {
        try {
            System.out.println("the currUser in AnnoucnementService:" + currUser);
            FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                    currUser,
                    /*"uh-settings:attributes:for-applications:uhgroupings:announcements",
                    "uh-settings:attributes:for-applications:uhgroupings:propertyString"*/
                    "uh-settings:attributes:for-applications:uhgroupings:propertyString",
                    "uh-settings:attributes:for-applications:uhgroupings"
            );
            System.out.println("done assigning findAttributesResults:" + findAttributesResults);
            return new Announcements(findAttributesResults);
        } catch (Exception e) {
            System.out.println("bad times man");
            return new Announcements();
        }
    }
    
}
