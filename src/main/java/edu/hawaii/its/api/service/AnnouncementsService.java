package edu.hawaii.its.api.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementsService {
    public static final Log log = LogFactory.getLog(AnnouncementsService.class);

    @Value("uh-settings:attributes:for-applications:uhgroupings:announcements") //may need to get rid of `:announcements`
    private String ANNOUNCEMENTS_ATTR_NAME;

    @Value("uh-settings:attributes:for-applications:uhgroupings:propertyString")
    private String ANNOUNCEMENTS_ATTR_DEF;

    @Autowired private GrouperApiService grouperApiService;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;


    public Announcements oneAnnouncement(/*String currentUser*/) {
        log.debug(String.format("oneAnnouncement; currentUser: none;"/*, currentUser*/));
        System.out.println("inside oneAnnouncements");
        FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                /*currentUser,*/
                ANNOUNCEMENTS_ATTR_DEF,
                ANNOUNCEMENTS_ATTR_NAME);
        return new Announcements(findAttributesResults);
    }


    private String resultCode;
    private List<Announcement> announcements;

//    public Announcements(FindAttributesResults findAttributesResults) {
//        setAnnouncements(findAttributesResults.getResults());
//        setResultCode(findAttributesResults.getResultCode());
//    }

//    public Announcements() {
//        System.out.println("empty param constructor for announcements");
//    }

//    public List<Announcement> getAnnouncements() {
//        return announcements;
//    }
//
//    public String getResultCode() {
//        return resultCode;
//    }

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
