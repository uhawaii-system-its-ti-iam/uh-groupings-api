package edu.hawaii.its.api.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.hawaii.its.api.type.Announcement;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class Announcements {
    private String resultCode;
    private List<Announcement> announcements;

    public Announcements(FindAttributesResults findAttributesResults) {
        findAnnouncements(findAttributesResults.getResults());
        setResultCode(findAttributesResults.getResultCode());
        setAnnouncements(findAttributesResults.getResults());
    }
    public Announcements() {
        findAnnouncements(new ArrayList<>());
        setResultCode("FAILURE");
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }
    public String getResultCode() {
        return resultCode;
    }
    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private void setAnnouncements(List<AttributesResult> attributesResults) {
        AttributesResult attributesResult = attributesResults.get(0);
        this.announcements = JsonUtil.asObjectList(attributesResult.getDescription(), Announcement.class); //getDefinition from getDescription (outputs null bc i think description needs to be a property)
    }

    /*public String getMessage() {
        List<Announcement> allAnnouncements = findAnnouncements().getAnnouncements();
        String valid = Announcement.validMessage(allAnnouncements);
        if (valid != "") {
            return valid;
        }
        return "";
    }*/

    /*
    public Announcements findAnnouncements() {
        try {
            FindAttributesResults findAttributesResults = grouperApiService.findAttributesResults(
                    "uh-settings:attributes:for-applications:uhgroupings:propertyString",
                    "uh-settings:attributes:for-applications:uhgroupings"
            );
            return new Announcements(findAttributesResults);
        } catch (Exception e) {
            return new Announcements();
        }
    } */

    private void findAnnouncements(List<AttributesResult> attributesResults) {
        this.announcements = new ArrayList<>();
        //hydrate this.announcements by creating Announcement objects that grab from the Ws
        for (AttributesResult attributesResult : attributesResults) {

            Announcement announcement =
                    JsonUtil.asObject(attributesResult.getDescription(), Announcement.class);
//            announcement.setName(attributesResult.getName());

            //don't think we need "replaceFirst"
//            announcement.setDescription(announcement.getDescription().replaceFirst("\\$\\{srhfgs}", this.groupingExtension));
//            //don't think we need synced - not 100% sure if we are setting the attr properties
//            announcement.setSynced(this.groupAttributes.stream()
//                    .anyMatch(groupAttribute -> groupAttribute.getAttributeName().equals(attributesResult.getName())));
            this.announcements.add(announcement);
        }
        //would want to compare here maybe? isolate the announcements here too? or can move this line out
//        this.announcements.sort(Comparator.comparing(Announcement::getDescription));
    }

    
}
