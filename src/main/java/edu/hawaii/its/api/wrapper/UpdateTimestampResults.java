package edu.hawaii.its.api.wrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;

import java.util.List;

public class UpdateTimestampResults extends AssignAttributesResults {

    public static final Log logger = LogFactory.getLog(UpdateTimestampResults.class);
    private static String FORMAT_STR = "yyyyMMdd'T'HHmm";

    public UpdateTimestampResults(
            WsAssignAttributesResults wsAssignAttributesResults) {
        super(wsAssignAttributesResults);
        logger.info("resultCode: " + getResultCode() + "; the date " + getReplacedTimestamp() + " was replaced with "
                + getUpdatedTimestamp() + "; ");
    }

    @Override public String getResultCode() {
        List<AttributeAssignValueResult> attributeAssignValueResultList = getValueResults();
        String result = "FAILURE";
        for (AttributeAssignValueResult attributeAssignValueResult : attributeAssignValueResultList) {
            result = "SUCCESS";
        }
        return result;
    }

    public String getReplacedTimestamp() {
        List<AttributeAssignValueResult> attributeAssignValueResultList = getValueResults();
        if (attributeAssignValueResultList.size() > 0) {
            return attributeAssignValueResultList.get(0).getValueSystem();
        }
        return "";
    }

    public String getUpdatedTimestamp() {
        List<AttributeAssignValueResult> attributeAssignValueResultList = getValueResults();
        if (attributeAssignValueResultList.size() == 2) {
            return attributeAssignValueResultList.get(1).getValueSystem();
        }
        return "";
    }
}
