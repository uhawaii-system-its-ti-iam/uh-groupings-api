package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {
    private List<Announcement> allAnnouncements;
    @Autowired
    private GrouperApiService grouperApiService;

    public String getMessage() {
        this.allAnnouncements = findAnnouncements().getAnnouncements();
        LocalDateTime currDate = LocalDateTime.now();

        //was run on a List
        for (int i = 0; i < this.allAnnouncements.size(); i++) {
            //convert dates here
            LocalDateTime dateTo = Dates.toLocalDateTime(this.allAnnouncements.get(i).getTo(), Dates.DATE_FORMAT_PLANNEDOUTAGE);
            LocalDateTime dateFrom = Dates.toLocalDateTime(this.allAnnouncements.get(i).getFrom(), Dates.DATE_FORMAT_PLANNEDOUTAGE);
            if (dateFrom.isBefore(currDate) && dateTo.isAfter(currDate)) {
                return this.allAnnouncements.get(i).getMessage();
            }
        }
        return "no message to display";
    }

    public Announcements findAnnouncements() {
        try {
            FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                    "uh-settings:attributes:for-applications:uhgroupings:propertyString",
                    "uh-settings:attributes:for-applications:uhgroupings"
            );
            System.out.println("done assigning findAttributesResults:" + findAttributesResults);
            return new Announcements(findAttributesResults);
        } catch (Exception e) {
            System.out.println("caught exception in AnnouncementService");
            return new Announcements();
        }
    }
    
}
